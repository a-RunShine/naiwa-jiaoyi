package com.campus.market.item;

import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.common.UserContext;
import com.campus.market.item.dto.CreateItemRequest;
import com.campus.market.item.dto.ItemQueryRequest;
import com.campus.market.item.entity.Item;
import com.campus.market.item.vo.ItemDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
@Tag(name = "商品")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "发布商品")
    public Result<ItemDetailVO> create(@RequestBody @Valid CreateItemRequest req) {
        return Result.ok(itemService.create(UserContext.require(), req));
    }

    @GetMapping
    @Operation(summary = "商品列表(白名单,支持筛选/排序/分页)")
    public Result<PageResult<Item>> list(@ModelAttribute ItemQueryRequest q) {
        return Result.ok(itemService.list(q));
    }

    @GetMapping("/{id}")
    @Operation(summary = "商品详情(白名单)")
    public Result<ItemDetailVO> detail(@PathVariable Long id) {
        return Result.ok(itemService.detail(id));
    }

    @PutMapping("/{id}/off")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "下架商品(仅卖家 + 仅 ON_SALE)")
    public Result<Item> offShelf(@PathVariable Long id) {
        return Result.ok(itemService.offShelf(UserContext.require(), id));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "删除商品(仅卖家,已成交不可删)")
    public Result<Void> delete(@PathVariable Long id) {
        itemService.delete(UserContext.require(), id);
        return Result.ok();
    }
}