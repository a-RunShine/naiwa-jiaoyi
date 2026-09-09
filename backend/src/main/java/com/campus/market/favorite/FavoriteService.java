package com.campus.market.favorite;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BusinessException;
import com.campus.market.common.ErrorCode;
import com.campus.market.common.PageResult;
import com.campus.market.favorite.entity.Favorite;
import com.campus.market.favorite.mapper.FavoriteMapper;
import com.campus.market.favorite.vo.FavoriteItemVO;
import com.campus.market.item.entity.Item;
import com.campus.market.item.mapper.ItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ItemMapper itemMapper;

    public FavoriteService(FavoriteMapper favoriteMapper, ItemMapper itemMapper) {
        this.favoriteMapper = favoriteMapper;
        this.itemMapper = itemMapper;
    }

    @Transactional
    public void add(Long uid, Long itemId) {
        if (itemMapper.selectById(itemId) == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }
        if (favoriteMapper.find(uid, itemId) != null) {
            throw new BusinessException(409, "已收藏");
        }
        Favorite f = new Favorite();
        f.setUserId(uid);
        f.setItemId(itemId);
        favoriteMapper.insert(f);
    }

    @Transactional
    public void remove(Long uid, Long itemId) {
        Favorite f = favoriteMapper.find(uid, itemId);
        if (f == null) {
            throw new BusinessException(404, "未收藏");
        }
        favoriteMapper.deleteById(f.getId());
    }

    public PageResult<FavoriteItemVO> list(Long uid, int page, int size) {
        // 直接分页查收藏(按 created_at DESC)
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, uid)
                .orderByDesc(Favorite::getCreatedAt);
        IPage<Favorite> p = new Page<>(page, size);
        IPage<Favorite> res = favoriteMapper.selectPage(p, wrapper);
        List<Favorite> favs = res.getRecords();
        if (favs.isEmpty()) {
            return PageResult.of(Collections.emptyList(), 0, page, size);
        }
        List<Long> itemIds = favs.stream().map(Favorite::getItemId).toList();
        List<Item> items = itemMapper.selectBatchIds(itemIds);
        Map<Long, Item> itemMap = items.stream().collect(Collectors.toMap(Item::getId, i -> i));
        List<FavoriteItemVO> vos = favs.stream()
                .map(f -> itemMap.get(f.getItemId()))
                .filter(java.util.Objects::nonNull)
                .map(FavoriteItemVO::from)
                .toList();
        return PageResult.of(vos, res.getTotal(), page, size);
    }
}