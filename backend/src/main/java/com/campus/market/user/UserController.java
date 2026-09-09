package com.campus.market.user;

import com.campus.market.common.Result;
import com.campus.market.common.UserContext;
import com.campus.market.user.dto.UpdateProfileRequest;
import com.campus.market.user.dto.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户")
    public Result<UserVO> me() {
        return Result.ok(userService.me(UserContext.require()));
    }

    @PutMapping("/me")
    @Operation(summary = "更新个人资料")
    public Result<UserVO> updateProfile(@RequestBody @Valid UpdateProfileRequest req) {
        return Result.ok(userService.updateProfile(UserContext.require(), req));
    }
}