package com.campus.market.wallet;

import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.common.UserContext;
import com.campus.market.wallet.dto.RechargeRequest;
import com.campus.market.wallet.vo.WalletFlowVO;
import com.campus.market.wallet.vo.WalletVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@Tag(name = "钱包")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/recharge")
    @Operation(summary = "充值")
    public Result<WalletVO> recharge(@RequestBody @Valid RechargeRequest req) {
        return Result.ok(walletService.recharge(UserContext.require(), req.amount()));
    }

    @GetMapping
    @Operation(summary = "我的钱包余额")
    public Result<WalletVO> me() {
        return Result.ok(walletService.me(UserContext.require()));
    }

    @GetMapping("/flows")
    @Operation(summary = "我的钱包流水")
    public Result<PageResult<WalletFlowVO>> flows(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(walletService.flows(UserContext.require(), page, size));
    }
}