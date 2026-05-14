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
import java.time.LocalDateTime;
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
    public Result<Object> confirm(@RequestBody Map<String, String> body,
                                  Authentication auth) {
        String voucherCode = body.get("voucherCode");
        String operatorId = auth.getName();
        String operatorName = authService.getUserName(operatorId);
        return Result.success(voucherService.verify(voucherCode, operatorId, operatorName));
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
}
