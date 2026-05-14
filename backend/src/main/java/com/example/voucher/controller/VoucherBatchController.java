package com.example.voucher.controller;

import com.example.voucher.common.Result;
import com.example.voucher.dto.CreateBatchRequest;
import com.example.voucher.dto.IssueVoucherRequest;
import com.example.voucher.service.VoucherBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
public class VoucherBatchController {

    private final VoucherBatchService batchService;
    private final com.example.voucher.service.AuthService authService;

    @PostMapping
    public Result<Object> create(@Valid @RequestBody CreateBatchRequest request,
                                  Authentication auth) {
        String operatorId = auth.getName();
        String operatorName = authService.getUserName(operatorId);
        return Result.success(batchService.create(request, operatorName));
    }

    @PostMapping("/issue")
    public Result<Object> issue(@Valid @RequestBody IssueVoucherRequest request,
                                 Authentication auth) {
        String operatorId = auth.getName();
        String operatorName = authService.getUserName(operatorId);
        return Result.success(batchService.issue(request, operatorId, operatorName));
    }

    @GetMapping
    public Result<Object> list() {
        return Result.success(batchService.list());
    }

    @GetMapping("/{id}")
    public Result<Object> getById(@PathVariable Long id) {
        return Result.success(batchService.getById(id));
    }

    @GetMapping("/{id}/vouchers")
    public Result<Object> getVouchers(@PathVariable Long id) {
        return Result.success(batchService.getVouchersByBatchId(id));
    }
}
