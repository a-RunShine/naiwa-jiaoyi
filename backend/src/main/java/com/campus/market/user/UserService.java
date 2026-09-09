package com.campus.market.user;

import com.campus.market.common.BusinessException;
import com.campus.market.common.ErrorCode;
import com.campus.market.user.dto.UpdateProfileRequest;
import com.campus.market.user.dto.UserVO;
import com.campus.market.user.entity.User;
import com.campus.market.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserVO me(Long uid) {
        User u = userMapper.selectById(uid);
        if (u == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return UserVO.from(u);
    }

    @Transactional
    public UserVO updateProfile(Long uid, UpdateProfileRequest req) {
        User u = userMapper.selectById(uid);
        if (u == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (req.nickname() != null) {
            u.setNickname(req.nickname());
        }
        if (req.avatarUrl() != null) {
            u.setAvatarUrl(req.avatarUrl());
        }
        userMapper.updateById(u);
        return UserVO.from(u);
    }
}