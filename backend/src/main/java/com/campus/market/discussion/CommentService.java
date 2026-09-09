package com.campus.market.discussion;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BusinessException;
import com.campus.market.common.ErrorCode;
import com.campus.market.common.PageResult;
import com.campus.market.discussion.dto.CommentCreateRequest;
import com.campus.market.discussion.entity.Comment;
import com.campus.market.discussion.mapper.CommentMapper;
import com.campus.market.discussion.vo.CommentVO;
import com.campus.market.item.entity.Item;
import com.campus.market.item.enums.ItemStatus;
import com.campus.market.item.mapper.ItemMapper;
import com.campus.market.user.dto.UserVO;
import com.campus.market.user.entity.User;
import com.campus.market.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    public CommentService(CommentMapper commentMapper, ItemMapper itemMapper, UserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.itemMapper = itemMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public CommentVO create(Long uid, Long itemId, CommentCreateRequest req) {
        if (req.content() == null || req.content().isBlank()) {
            throw new BusinessException(400, "内容不能为空");
        }
        Item it = itemMapper.selectById(itemId);
        if (it == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }
        if (it.getStatus() != ItemStatus.ON_SALE) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        if (req.parentId() != null) {
            Comment parent = commentMapper.selectById(req.parentId());
            if (parent == null || !parent.getItemId().equals(itemId)) {
                throw new BusinessException(400, "父留言不存在或不属于该商品");
            }
        }
        Comment c = new Comment();
        c.setItemId(itemId);
        c.setUserId(uid);
        c.setParentId(req.parentId());
        c.setContent(req.content());
        commentMapper.insert(c);
        User author = userMapper.selectById(uid);
        return CommentVO.from(c, author == null ? null : UserVO.from(author));
    }

    public PageResult<CommentVO> list(Long itemId, int page, int size) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getItemId, itemId)
                .orderByAsc(Comment::getCreatedAt);
        IPage<Comment> p = new Page<>(page, size);
        IPage<Comment> res = commentMapper.selectPage(p, wrapper);
        List<Comment> records = res.getRecords();
        if (records.isEmpty()) {
            return PageResult.of(List.of(), 0, page, size);
        }
        List<Long> userIds = records.stream().map(Comment::getUserId).distinct().toList();
        Map<Long, UserVO> userMap = new HashMap<>();
        for (User u : userMapper.selectBatchIds(userIds)) {
            userMap.put(u.getId(), UserVO.from(u));
        }
        List<CommentVO> vos = records.stream()
                .map(c -> CommentVO.from(c, userMap.get(c.getUserId())))
                .toList();
        return PageResult.of(vos, res.getTotal(), page, size);
    }
}