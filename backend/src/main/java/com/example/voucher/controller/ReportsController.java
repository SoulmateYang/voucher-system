package com.example.voucher.controller;

import com.example.voucher.common.Result;
import com.example.voucher.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportService reportService;

    @GetMapping("/summary")
    public Result<Object> summary(@RequestParam String startDate,
                                  @RequestParam String endDate) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        return Result.success(reportService.getSummary(start, end));
    }

    @GetMapping("/daily-trend")
    public Result<Object> dailyTrend(@RequestParam String startDate,
                                     @RequestParam String endDate) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
        return Result.success(reportService.getDailyTrend(start, end));
    }
}
