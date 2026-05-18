package com.example.voucher.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    private final LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
    private final LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59);

    @Test
    void getStats_shouldReturnResultWithKeys() {
        var result = reportService.getStats(start, end);
        assertNotNull(result);
        assertNotNull(result.get("statusCounts"));
        assertNotNull(result.get("dailyIssue"));
        assertNotNull(result.get("dailyVerify"));
    }

    @Test
    void getSummary_shouldReturnNumericCounts() {
        var result = reportService.getSummary(start, end);
        assertNotNull(result);
        assertTrue(result.containsKey("totalIssued"));
        assertTrue(result.containsKey("totalUsed"));
        assertTrue(result.containsKey("totalExpired"));
        assertTrue(result.containsKey("totalCancelled"));
    }

    @Test
    void getDailyTrend_shouldReturnList() {
        var result = reportService.getDailyTrend(start, end);
        assertNotNull(result);
        assertInstanceOf(java.util.List.class, result);
    }
}
