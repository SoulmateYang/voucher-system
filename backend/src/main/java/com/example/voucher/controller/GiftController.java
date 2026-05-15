package com.example.voucher.controller;

import com.example.voucher.common.Result;
import com.example.voucher.dto.GiftVoucherRequest;
import com.example.voucher.service.AuthService;
import com.example.voucher.service.GiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vouchers/my/gift")
@RequiredArgsConstructor
public class GiftController {

    private final GiftService giftService;
    private final AuthService authService;

    @PostMapping
    public Result<Object> gift(@Valid @RequestBody GiftVoucherRequest request,
                                Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String userId = user != null ? user.getUsername() : auth.getName();
        String userName = user != null ? user.getRealName() : auth.getName();
        return Result.success(giftService.gift(
            request.getVoucherId(), userId, userName,
            request.getToEmployeeId(), request.getMessage()
        ));
    }

    @PostMapping("/{giftId}/cancel")
    public Result<Void> cancelGift(@PathVariable Long giftId, Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String userId = user != null ? user.getUsername() : auth.getName();
        giftService.cancelGift(giftId, userId);
        return Result.success();
    }

    @GetMapping("/inbox")
    public Result<Object> inbox(Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String userId = user != null ? user.getUsername() : auth.getName();
        return Result.success(giftService.getInbox(userId));
    }

    @PostMapping("/{giftId}/accept")
    public Result<Void> accept(@PathVariable Long giftId, Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String userId = user != null ? user.getUsername() : auth.getName();
        giftService.acceptGift(giftId, userId);
        return Result.success();
    }

    @PostMapping("/{giftId}/reject")
    public Result<Void> reject(@PathVariable Long giftId, Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String userId = user != null ? user.getUsername() : auth.getName();
        giftService.rejectGift(giftId, userId);
        return Result.success();
    }

    @GetMapping("/outbox")
    public Result<Object> outbox(Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String userId = user != null ? user.getUsername() : auth.getName();
        return Result.success(giftService.getOutbox(userId));
    }
}
