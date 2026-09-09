package com.campus.market.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BusinessException;
import com.campus.market.common.ErrorCode;
import com.campus.market.common.PageResult;
import com.campus.market.item.entity.Item;
import com.campus.market.item.enums.ItemStatus;
import com.campus.market.item.enums.TradeMethod;
import com.campus.market.item.mapper.ItemMapper;
import com.campus.market.order.dto.CreateOrderRequest;
import com.campus.market.order.dto.OrderQueryRequest;
import com.campus.market.order.entity.Order;
import com.campus.market.order.enums.OrderStatus;
import com.campus.market.order.enums.OrderTradeMethod;
import com.campus.market.order.mapper.OrderMapper;
import com.campus.market.order.vo.OrderVO;
import com.campus.market.wallet.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 订单状态机核心(对齐 plan.md §订单模块 §模块交互 §3-§4)。
 *
 * <ul>
 *   <li>create:FOR UPDATE 行锁 + 状态联动 + ESCROW 冻结钱包</li>
 *   <li>confirm/reject/cancel/complete:再次 FOR UPDATE 锁定 item,保证状态原子</li>
 *   <li>complete:幂等(status==COMPLETED 直接返,不重复 settle)</li>
 * </ul>
 */
@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final ItemMapper itemMapper;
    private final WalletService walletService;

    public OrderService(OrderMapper orderMapper, ItemMapper itemMapper, WalletService walletService) {
        this.orderMapper = orderMapper;
        this.itemMapper = itemMapper;
        this.walletService = walletService;
    }

    @Transactional
    public OrderVO create(Long buyerId, CreateOrderRequest req) {
        Item it = itemMapper.selectByIdForUpdate(req.itemId());
        if (it == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }
        if (it.getSellerId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (it.getStatus() != ItemStatus.ON_SALE) {
            throw new BusinessException(ErrorCode.ITEM_ALREADY_RESERVED);
        }
        OrderTradeMethod otm = req.tradeMethod();
        boolean ok = switch (it.getTradeMethod()) {
            case OFFLINE_ONLY -> otm == OrderTradeMethod.OFFLINE;
            case ESCROW_ONLY -> otm == OrderTradeMethod.ESCROW;
            case BOTH -> true;
        };
        if (!ok) {
            throw new BusinessException(ErrorCode.TRADE_METHOD_MISMATCH);
        }

        Order o = new Order();
        o.setItemId(it.getId());
        o.setSellerId(it.getSellerId());
        o.setBuyerId(buyerId);
        o.setPriceSnapshot(it.getPrice());
        o.setTradeMethod(otm);
        o.setStatus(OrderStatus.PENDING);
        orderMapper.insert(o);

        it.setStatus(ItemStatus.RESERVED);
        itemMapper.updateById(it);

        if (otm == OrderTradeMethod.ESCROW) {
            walletService.freeze(buyerId, o.getId(), it.getPrice());
        }
        return OrderVO.from(o);
    }

    @Transactional
    public OrderVO confirm(Long sellerId, Long orderId) {
        Order o = must(orderId);
        if (!o.getSellerId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (o.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        o.setStatus(OrderStatus.PENDING_HANDOVER);
        orderMapper.updateById(o);
        return OrderVO.from(o);
    }

    @Transactional
    public OrderVO reject(Long sellerId, Long orderId) {
        Order o = must(orderId);
        if (!o.getSellerId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (o.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        Item it = itemMapper.selectByIdForUpdate(o.getItemId());
        o.setStatus(OrderStatus.REJECTED);
        orderMapper.updateById(o);
        it.setStatus(ItemStatus.ON_SALE);
        itemMapper.updateById(it);
        if (o.getTradeMethod() == OrderTradeMethod.ESCROW) {
            walletService.unfreeze(o.getBuyerId(), o.getId(), o.getPriceSnapshot());
        }
        return OrderVO.from(o);
    }

    @Transactional
    public OrderVO cancel(Long uid, Long orderId) {
        Order o = must(orderId);
        boolean party = uid.equals(o.getBuyerId()) || uid.equals(o.getSellerId());
        if (!party) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (o.getStatus() != OrderStatus.PENDING && o.getStatus() != OrderStatus.PENDING_HANDOVER) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        Item it = itemMapper.selectByIdForUpdate(o.getItemId());
        o.setStatus(OrderStatus.CANCELLED);
        orderMapper.updateById(o);
        it.setStatus(ItemStatus.ON_SALE);
        itemMapper.updateById(it);
        if (o.getTradeMethod() == OrderTradeMethod.ESCROW) {
            walletService.unfreeze(o.getBuyerId(), o.getId(), o.getPriceSnapshot());
        }
        return OrderVO.from(o);
    }

    @Transactional
    public OrderVO complete(Long buyerId, Long orderId) {
        Order o = must(orderId);
        if (!o.getBuyerId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (o.getStatus() == OrderStatus.COMPLETED) {
            // 幂等
            return OrderVO.from(o);
        }
        if (o.getStatus() != OrderStatus.PENDING_HANDOVER) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        Item it = itemMapper.selectByIdForUpdate(o.getItemId());
        o.setStatus(OrderStatus.COMPLETED);
        o.setCompletedAt(LocalDateTime.now());
        orderMapper.updateById(o);
        it.setStatus(ItemStatus.SOLD);
        itemMapper.updateById(it);
        if (o.getTradeMethod() == OrderTradeMethod.ESCROW) {
            walletService.settle(o.getBuyerId(), o.getSellerId(), o.getId(), o.getPriceSnapshot());
        }
        return OrderVO.from(o);
    }

    public PageResult<OrderVO> list(Long uid, OrderQueryRequest q) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if ("buy".equalsIgnoreCase(q.getRole())) {
            wrapper.eq(Order::getBuyerId, uid);
        } else if ("sell".equalsIgnoreCase(q.getRole())) {
            wrapper.eq(Order::getSellerId, uid);
        } else {
            throw new BusinessException(400, "role 必须为 buy 或 sell");
        }
        if (q.getStatus() != null) {
            wrapper.eq(Order::getStatus, q.getStatus());
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        IPage<Order> p = new Page<>(q.getPage(), q.getSize());
        IPage<Order> res = orderMapper.selectPage(p, wrapper);
        return PageResult.of(res.getRecords().stream().map(OrderVO::from).toList(),
                res.getTotal(), q.getPage(), q.getSize());
    }

    private Order must(Long id) {
        Order o = orderMapper.selectById(id);
        if (o == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return o;
    }
}