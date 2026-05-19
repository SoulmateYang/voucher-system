package com.example.voucher.controller;

import com.example.voucher.common.Result;
import com.example.voucher.service.ReportService;
import com.example.voucher.service.VoucherService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;
    private final ReportService reportService;
    private final com.example.voucher.service.AuthService authService;

    @PostMapping("/lookup")
    public Result<Object> lookup(@RequestBody Map<String, String> body) {
        String voucherCode = body.get("voucherCode");
        return Result.success(voucherService.lookup(voucherCode));
    }

    @PostMapping("/confirm")
    public Result<Object> confirm(@RequestBody Map<String, Object> body,
                                  Authentication auth) {
        String voucherCode = (String) body.get("voucherCode");
        String operatorId = auth.getName();
        String operatorName = authService.getUserName(operatorId);
        java.math.BigDecimal orderAmount = null;
        if (body.get("orderAmount") != null) {
            orderAmount = new java.math.BigDecimal(body.get("orderAmount").toString());
        }
        return Result.success(voucherService.verify(voucherCode, operatorId, operatorName, orderAmount));
    }

    @GetMapping("/today-records")
    public Result<Object> todayRecords(Authentication auth) {
        return Result.success(voucherService.getTodayRecords(auth.getName()));
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, Authentication auth) {
        String operatorId = auth.getName();
        String operatorName = authService.getUserName(operatorId);
        voucherService.cancel(id, operatorId, operatorName);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Object> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        java.time.LocalDateTime expireAt = null;
        if (body.get("expireAt") != null) {
            String expireStr = body.get("expireAt").toString();
            expireAt = java.time.LocalDateTime.parse(expireStr.replace("Z", ""));
        }
        String remark = (String) body.get("remark");
        return Result.success(voucherService.updateVoucher(id, expireAt, remark));
    }

    @PutMapping("/{id}/category")
    public Result<Void> assignCategory(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long categoryId = body.get("categoryId") != null
            ? Long.valueOf(body.get("categoryId").toString())
            : null;
        voucherService.assignCategory(id, categoryId);
        return Result.success();
    }

    @PutMapping("/category/batch")
    public Result<Object> batchAssignCategory(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        java.util.List<Integer> rawIds = (java.util.List<Integer>) body.get("voucherIds");
        java.util.List<Long> voucherIds = new java.util.ArrayList<>();
        if (rawIds != null) {
            for (Integer i : rawIds) {
                voucherIds.add(i.longValue());
            }
        }
        Long categoryId = body.get("categoryId") != null
            ? Long.valueOf(body.get("categoryId").toString())
            : null;
        int affected = voucherService.batchAssignCategory(voucherIds, categoryId);
        return Result.success(java.util.Map.of("affectedCount", affected));
    }

    @GetMapping("/{id}")
    public Result<Object> getById(@PathVariable Long id) {
        return Result.success(voucherService.getById(id));
    }

    @GetMapping("/report")
    public Result<Object> report(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return Result.success(reportService.getStats(start, end));
    }

    @GetMapping("/export")
    public void export(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            HttpServletResponse response) throws IOException {
        reportService.exportExcel(response, start, end);
    }

    @GetMapping
    public Result<Object> list(
            @RequestParam(required = false) String holderId,
            @RequestParam(required = false) String holderName,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String voucherType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate expireStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate expireEnd,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        LocalDateTime startDateTime = expireStart != null ? expireStart.atStartOfDay() : null;
        LocalDateTime endDateTime = expireEnd != null ? expireEnd.atTime(LocalTime.MAX) : null;
        return Result.success(voucherService.listAllPaged(page, pageSize,
            holderId, holderName, keyword, voucherType, startDateTime, endDateTime, categoryId));
    }
}
