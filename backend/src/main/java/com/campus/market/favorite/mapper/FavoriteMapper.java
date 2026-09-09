package com.campus.market.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.market.favorite.entity.Favorite;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT * FROM favorite WHERE user_id = #{uid} AND item_id = #{itemId}")
    Favorite find(@Param("uid") Long uid, @Param("itemId") Long itemId);

    @Select("SELECT item_id FROM favorite WHERE user_id = #{uid} ORDER BY created_at DESC")
    List<Long> findItemIdsByUser(@Param("uid") Long uid);
}