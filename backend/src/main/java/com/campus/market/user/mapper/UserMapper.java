package com.campus.market.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.market.user.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE open_id = #{openId} LIMIT 1")
    Optional<User> findByOpenId(@Param("openId") String openId);
}