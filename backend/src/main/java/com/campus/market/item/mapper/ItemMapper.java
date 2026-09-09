package com.campus.market.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.market.item.dto.ItemQueryRequest;
import com.campus.market.item.entity.Item;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ItemMapper extends BaseMapper<Item> {

    /**
     * 行锁查商品(下单 / 改状态时用)。
     */
    @Select("SELECT * FROM item WHERE id = #{id} FOR UPDATE")
    Item selectByIdForUpdate(@Param("id") Long id);

    /**
     * 列表筛选 + 排序(动态 SQL 写在 ItemMapper.xml)。
     */
    IPage<Item> queryItems(IPage<Item> page, @Param("q") ItemQueryRequest q);
}