package com.campus.market.favorite;

import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.common.UserContext;
import com.campus.market.favorite.vo.FavoriteItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "收藏")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{itemId}")
    @Operation(summary = "收藏商品")
    public Result<Void> add(@PathVariable Long itemId) {
        favoriteService.add(UserContext.require(), itemId);
        return Result.ok();
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "取消收藏")
    public Result<Void> remove(@PathVariable Long itemId) {
        favoriteService.remove(UserContext.require(), itemId);
        return Result.ok();
    }

    @GetMapping
    @Operation(summary = "我的收藏")
    public Result<PageResult<FavoriteItemVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(favoriteService.list(UserContext.require(), page, size));
    }
}