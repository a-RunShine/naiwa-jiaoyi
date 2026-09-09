package com.campus.market.item;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BusinessException;
import com.campus.market.common.ErrorCode;
import com.campus.market.common.PageResult;
import com.campus.market.common.UserContext;
import com.campus.market.item.dto.CreateItemRequest;
import com.campus.market.item.dto.ItemQueryRequest;
import com.campus.market.item.entity.Item;
import com.campus.market.item.enums.ItemStatus;
import com.campus.market.item.mapper.ItemMapper;
import com.campus.market.item.vo.ItemDetailVO;
import com.campus.market.user.dto.UserVO;
import com.campus.market.user.entity.User;
import com.campus.market.user.mapper.UserMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class ItemService {

    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public ItemService(ItemMapper itemMapper, UserMapper userMapper, ObjectMapper objectMapper) {
        this.itemMapper = itemMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ItemDetailVO create(Long sellerId, CreateItemRequest req) {
        Item it = new Item();
        it.setSellerId(sellerId);
        it.setTitle(req.title());
        it.setDescription(req.description());
        it.setCategory(req.category());
        it.setCondition(req.condition());
        it.setPrice(req.price());
        it.setTradeMethod(req.tradeMethod());
        it.setStatus(ItemStatus.ON_SALE);
        try {
            it.setImages(objectMapper.writeValueAsString(req.images()));
        } catch (Exception e) {
            throw new BusinessException(500, "图片序列化失败");
        }
        itemMapper.insert(it);
        return detailInternal(it);
    }

    public PageResult<Item> list(ItemQueryRequest q) {
        int pg = q.pageOrDefault();
        int sz = q.sizeOrDefault();
        ItemQueryRequest effective = q;
        if ("me".equalsIgnoreCase(q.getSellerId())) {
            Long uid = UserContext.get();
            if (uid == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
            ItemQueryRequest clone = new ItemQueryRequest();
            clone.setCategory(q.getCategory());
            clone.setCondition(q.getCondition());
            clone.setTradeMethod(q.getTradeMethod());
            clone.setMinPrice(q.getMinPrice());
            clone.setMaxPrice(q.getMaxPrice());
            clone.setKeyword(q.getKeyword());
            clone.setSort(q.getSort());
            clone.setSellerId(String.valueOf(uid));
            clone.setPage(pg);
            clone.setSize(sz);
            effective = clone;
        }
        IPage<Item> page = new Page<>(pg, sz);
        IPage<Item> res = itemMapper.queryItems(page, effective);
        return PageResult.of(res.getRecords(), res.getTotal(), pg, sz);
    }

    public ItemDetailVO detail(Long id) {
        Item it = itemMapper.selectById(id);
        if (it == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }
        return detailInternal(it);
    }

    /**
     * 我的发布(脱敏掉状态过滤)。
     */
    public PageResult<Item> listMine(Long uid, int page, int size) {
        ItemQueryRequest q = new ItemQueryRequest();
        q.setSellerId(String.valueOf(uid));
        q.setPage(page);
        q.setSize(size);
        return list(q);
    }

    @Transactional
    public Item offShelf(Long uid, Long id) {
        Item it = itemMapper.selectById(id);
        if (it == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }
        if (!it.getSellerId().equals(uid)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (it.getStatus() != ItemStatus.ON_SALE) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        it.setStatus(ItemStatus.OFF_SHELF);
        itemMapper.updateById(it);
        return it;
    }

    @Transactional
    public void delete(Long uid, Long id) {
        Item it = itemMapper.selectById(id);
        if (it == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }
        if (!it.getSellerId().equals(uid)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (it.getStatus() == ItemStatus.SOLD) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        itemMapper.deleteById(id);
    }

    private ItemDetailVO detailInternal(Item it) {
        User seller = userMapper.selectById(it.getSellerId());
        List<String> imgs;
        try {
            if (it.getImages() == null || it.getImages().isBlank()) {
                imgs = Collections.emptyList();
            } else {
                imgs = objectMapper.readValue(it.getImages(), new TypeReference<List<String>>() {});
            }
        } catch (Exception e) {
            imgs = Collections.emptyList();
        }
        return ItemDetailVO.of(it, seller == null ? null : UserVO.from(seller), imgs);
    }
}