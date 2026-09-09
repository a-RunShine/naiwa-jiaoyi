package com.campus.market.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.market.review.entity.Review;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ReviewMapper extends BaseMapper<Review> {

    @Select("SELECT COUNT(1) FROM review WHERE order_id = #{orderId} AND author_id = #{authorId}")
    long existsByOrderAndAuthor(@Param("orderId") Long orderId, @Param("authorId") Long authorId);
}