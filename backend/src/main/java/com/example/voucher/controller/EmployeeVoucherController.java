package com.example.voucher.controller;

import com.example.voucher.common.Result;
import com.example.voucher.entity.Voucher;
import com.example.voucher.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vouchers/my")
@RequiredArgsConstructor
public class EmployeeVoucherController {

    private final VoucherService voucherService;
    private final com.example.voucher.service.AuthService authService;

    @GetMapping
    public Result<Object> list(Authentication auth,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "20") int size) {
        var user = authService.getCurrentUser(auth.getName());
        String holderId = user != null ? user.getUsername() : auth.getName();
        return Result.success(voucherService.listByHolderPaged(holderId, page, size));
    }

    @GetMapping("/{id}")
    public Result<Object> getById(@PathVariable Long id, Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String holderId = user != null ? user.getUsername() : auth.getName();
        Voucher voucher = voucherService.getById(id);
        if (voucher == null || !voucher.getHolderId().equals(holderId)) {
            return Result.error(403, "无权查看该券");
        }
        return Result.success(voucher);
    }
}
