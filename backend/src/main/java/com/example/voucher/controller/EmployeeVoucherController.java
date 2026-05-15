package com.example.voucher.controller;

import com.example.voucher.common.Result;
import com.example.voucher.dto.ManualVoucherRequest;
import com.example.voucher.entity.Voucher;
import com.example.voucher.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
                                @RequestParam(defaultValue = "20") int size,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String voucherType) {
        var user = authService.getCurrentUser(auth.getName());
        String holderId = user != null ? user.getUsername() : auth.getName();
        return Result.success(voucherService.listByHolderPaged(holderId, page, size, keyword, status, voucherType));
    }

    @GetMapping("/{id}")
    public Result<Object> getById(@PathVariable Long id, Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String holderId = user != null ? user.getUsername() : auth.getName();
        Voucher voucher = voucherService.getById(id);
        if (voucher == null || !voucher.getHolderId().equals(holderId)) {
            return Result.error(403, "无权查看该券");
        }
        return Result.success(voucherService.getDetailById(id));
    }

    @PostMapping("/{id}/favorite")
    public Result<Void> toggleFavorite(@PathVariable Long id, Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String holderId = user != null ? user.getUsername() : auth.getName();
        voucherService.toggleFavorite(id, holderId);
        return Result.success();
    }

    @PostMapping("/{id}/pin")
    public Result<Void> togglePin(@PathVariable Long id, Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String holderId = user != null ? user.getUsername() : auth.getName();
        voucherService.togglePin(id, holderId);
        return Result.success();
    }

    @PostMapping("/manual")
    public Result<Object> addManual(@RequestBody ManualVoucherRequest request,
                                     Authentication auth) {
        var user = authService.getCurrentUser(auth.getName());
        String holderId = user != null ? user.getUsername() : auth.getName();
        String holderName = user != null ? user.getRealName() : auth.getName();

        Voucher voucher = new Voucher();
        voucher.setRemark(request.getName());
        voucher.setFaceValue(request.getFaceValue());
        if (request.getExpireAt() != null && !request.getExpireAt().isEmpty()) {
            voucher.setExpireAt(LocalDate.parse(request.getExpireAt(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(LocalTime.MAX));
        }
        return Result.success(voucherService.addManual(voucher, holderId, holderName));
    }
}
