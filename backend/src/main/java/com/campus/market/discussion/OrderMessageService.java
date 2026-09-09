package com.campus.market.discussion;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BusinessException;
import com.campus.market.common.ErrorCode;
import com.campus.market.common.PageResult;
import com.campus.market.discussion.entity.OrderMessage;
import com.campus.market.discussion.mapper.OrderMessageMapper;
import com.campus.market.discussion.vo.OrderMessageVO;
import com.campus.market.order.entity.Order;
import com.campus.market.order.mapper.OrderMapper;
import com.campus.market.user.dto.UserVO;
import com.campus.market.user.entity.User;
import com.campus.market.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderMessageService {

    private final OrderMessageMapper orderMessageMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    public OrderMessageService(OrderMessageMapper orderMessageMapper,
                               OrderMapper orderMapper,
                               UserMapper userMapper) {
        this.orderMessageMapper = orderMessageMapper;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public OrderMessageVO create(Long uid, Long orderId, String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(400, "内容不能为空");
        }
        Order o = orderMapper.selectById(orderId);
        if (o == null) {
            throw new BusinessException(404, "订单不存在");
        }
        checkParty(uid, o);
        OrderMessage m = new OrderMessage();
        m.setOrderId(orderId);
        m.setSenderId(uid);
        m.setContent(content);
        orderMessageMapper.insert(m);
        User sender = userMapper.selectById(uid);
        return OrderMessageVO.from(m, sender == null ? null : UserVO.from(sender));
    }

    public PageResult<OrderMessageVO> list(Long uid, Long orderId, LocalDateTime since, int page, int size) {
        Order o = orderMapper.selectById(orderId);
        if (o == null) {
            throw new BusinessException(404, "订单不存在");
        }
        checkParty(uid, o);
        LambdaQueryWrapper<OrderMessage> wrapper = new LambdaQueryWrapper<OrderMessage>()
                .eq(OrderMessage::getOrderId, orderId)
                .gt(since != null, OrderMessage::getCreatedAt, since)
                .orderByAsc(OrderMessage::getCreatedAt);
        IPage<OrderMessage> p = new Page<>(page, size);
        IPage<OrderMessage> res = orderMessageMapper.selectPage(p, wrapper);
        List<OrderMessage> records = res.getRecords();
        if (records.isEmpty()) {
            return PageResult.of(List.of(), 0, page, size);
        }
        List<Long> userIds = records.stream().map(OrderMessage::getSenderId).distinct().toList();
        Map<Long, UserVO> userMap = new HashMap<>();
        for (User u : userMapper.selectBatchIds(userIds)) {
            userMap.put(u.getId(), UserVO.from(u));
        }
        List<OrderMessageVO> vos = records.stream()
                .map(m -> OrderMessageVO.from(m, userMap.get(m.getSenderId())))
                .toList();
        return PageResult.of(vos, res.getTotal(), page, size);
    }

    private void checkParty(Long uid, Order o) {
        if (!uid.equals(o.getBuyerId()) && !uid.equals(o.getSellerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}