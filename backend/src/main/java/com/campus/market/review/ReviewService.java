package com.campus.market.review;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BusinessException;
import com.campus.market.common.ErrorCode;
import com.campus.market.common.PageResult;
import com.campus.market.order.entity.Order;
import com.campus.market.order.enums.OrderStatus;
import com.campus.market.order.mapper.OrderMapper;
import com.campus.market.review.dto.ReviewCreateRequest;
import com.campus.market.review.entity.Review;
import com.campus.market.review.mapper.ReviewMapper;
import com.campus.market.review.vo.ReviewVO;
import com.campus.market.user.entity.User;
import com.campus.market.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    public ReviewService(ReviewMapper reviewMapper, OrderMapper orderMapper, UserMapper userMapper) {
        this.reviewMapper = reviewMapper;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public ReviewVO create(Long uid, Long orderId, ReviewCreateRequest req) {
        Order o = orderMapper.selectById(orderId);
        if (o == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!uid.equals(o.getBuyerId()) && !uid.equals(o.getSellerId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (o.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        if (reviewMapper.existsByOrderAndAuthor(orderId, uid) > 0) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        Long targetId = uid.equals(o.getBuyerId()) ? o.getSellerId() : o.getBuyerId();
        Review r = new Review();
        r.setOrderId(orderId);
        r.setAuthorId(uid);
        r.setTargetUserId(targetId);
        r.setRating(req.rating());
        r.setContent(req.content() == null ? "" : req.content());
        reviewMapper.insert(r);
        User author = userMapper.selectById(uid);
        return ReviewVO.from(r, author == null ? null : author.getNickname());
    }

    public PageResult<ReviewVO> listByOrder(Long orderId, int page, int size) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, orderId)
                .orderByAsc(Review::getCreatedAt);
        IPage<Review> p = new Page<>(page, size);
        IPage<Review> res = reviewMapper.selectPage(p, wrapper);
        return enrich(res.getRecords(), res.getTotal(), page, size);
    }

    public PageResult<ReviewVO> receivedByUser(Long targetUserId, int page, int size) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .eq(Review::getTargetUserId, targetUserId)
                .orderByDesc(Review::getCreatedAt);
        IPage<Review> p = new Page<>(page, size);
        IPage<Review> res = reviewMapper.selectPage(p, wrapper);
        return enrich(res.getRecords(), res.getTotal(), page, size);
    }

    private PageResult<ReviewVO> enrich(List<Review> records, long total, int page, int size) {
        if (records.isEmpty()) {
            return PageResult.of(List.of(), total, page, size);
        }
        List<Long> userIds = records.stream().map(Review::getAuthorId).distinct().toList();
        Map<Long, String> nameMap = new HashMap<>();
        for (User u : userMapper.selectBatchIds(userIds)) {
            nameMap.put(u.getId(), u.getNickname());
        }
        List<ReviewVO> vos = records.stream()
                .map(r -> ReviewVO.from(r, nameMap.get(r.getAuthorId())))
                .toList();
        return PageResult.of(vos, total, page, size);
    }
}