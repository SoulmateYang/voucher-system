package com.example.voucher.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.example.voucher.entity.Voucher;
import com.example.voucher.mapper.VoucherMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final VoucherMapper voucherMapper;

    public Map<String, Object> getStats(LocalDateTime start, LocalDateTime end) {
        List<Map<String, Object>> statusCounts = voucherMapper.countByStatus(start, end);
        List<Map<String, Object>> dailyIssue = voucherMapper.dailyIssueCount(start, end);
        List<Map<String, Object>> dailyVerify = voucherMapper.dailyVerifyCount(start, end);

        Map<String, Object> result = new HashMap<>();
        result.put("statusCounts", statusCounts);
        result.put("dailyIssue", dailyIssue);
        result.put("dailyVerify", dailyVerify);
        return result;
    }

    public Map<String, Object> getSummary(LocalDateTime start, LocalDateTime end) {
        List<Map<String, Object>> statusCounts = voucherMapper.countByStatus(start, end);
        long totalIssued = 0, totalUsed = 0, totalExpired = 0, totalCancelled = 0;
        for (Map<String, Object> row : statusCounts) {
            Object status = row.get("status");
            Object cnt = row.get("cnt");
            long count = cnt instanceof Number ? ((Number) cnt).longValue() : 0L;
            if ("ISSUED".equals(status)) {
                totalIssued = count;
            } else if ("USED".equals(status)) {
                totalUsed = count;
            } else if ("EXPIRED".equals(status)) {
                totalExpired = count;
            } else if ("CANCELLED".equals(status)) {
                totalCancelled = count;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("totalIssued", totalIssued);
        result.put("totalUsed", totalUsed);
        result.put("totalExpired", totalExpired);
        result.put("totalCancelled", totalCancelled);
        return result;
    }

    public List<Map<String, Object>> getDailyTrend(LocalDateTime start, LocalDateTime end) {
        List<Map<String, Object>> dailyIssue = voucherMapper.dailyIssueCount(start, end);
        List<Map<String, Object>> dailyVerify = voucherMapper.dailyVerifyCount(start, end);
        List<Map<String, Object>> dailyExpired = voucherMapper.dailyExpiredCount(start, end);
        List<Map<String, Object>> dailyCancelled = voucherMapper.dailyCancelledCount(start, end);

        Map<String, Map<String, Object>> merged = new java.util.LinkedHashMap<>();
        for (List<Map<String, Object>> list : List.of(dailyIssue, dailyVerify, dailyExpired, dailyCancelled)) {
            for (Map<String, Object> row : list) {
                String dt = row.get("dt").toString();
                merged.putIfAbsent(dt, new HashMap<>(Map.of(
                    "date", dt, "issuedCount", 0L, "usedCount", 0L,
                    "expiredCount", 0L, "cancelledCount", 0L, "totalValid", 0L
                )));
            }
        }
        for (Map<String, Object> row : dailyIssue) {
            String dt = row.get("dt").toString();
            Map<String, Object> m = merged.get(dt);
            if (m != null) m.put("issuedCount", toLong(row.get("cnt")));
        }
        for (Map<String, Object> row : dailyVerify) {
            String dt = row.get("dt").toString();
            Map<String, Object> m = merged.get(dt);
            if (m != null) m.put("usedCount", toLong(row.get("cnt")));
        }
        for (Map<String, Object> row : dailyExpired) {
            String dt = row.get("dt").toString();
            Map<String, Object> m = merged.get(dt);
            if (m != null) m.put("expiredCount", toLong(row.get("cnt")));
        }
        for (Map<String, Object> row : dailyCancelled) {
            String dt = row.get("dt").toString();
            Map<String, Object> m = merged.get(dt);
            if (m != null) m.put("cancelledCount", toLong(row.get("cnt")));
        }

        long cumIssued = 0, cumUsed = 0, cumExpired = 0, cumCancelled = 0;
        for (Map<String, Object> m : merged.values()) {
            cumIssued += (long) m.get("issuedCount");
            cumUsed += (long) m.get("usedCount");
            cumExpired += (long) m.get("expiredCount");
            cumCancelled += (long) m.get("cancelledCount");
            m.put("totalValid", cumIssued - cumUsed - cumExpired - cumCancelled);
        }
        return new ArrayList<>(merged.values());
    }

    private long toLong(Object v) {
        return v instanceof Number ? ((Number) v).longValue() : 0L;
    }

    public void exportExcel(HttpServletResponse response, LocalDateTime start, LocalDateTime end)
            throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String fileName = URLEncoder.encode("卡券统计报表.xlsx", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        List<ExportRow> rows = new ArrayList<>();
        // Collect data from vouchers
        List<Voucher> vouchers = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .between(Voucher::getCreatedAt, start, end)
        );
        for (Voucher v : vouchers) {
            ExportRow row = new ExportRow();
            row.setVoucherCode(v.getVoucherCode());
            row.setHolderName(v.getHolderName());
            row.setStatus(v.getStatus());
            row.setIssuedAt(v.getIssuedAt() != null ? v.getIssuedAt().toString() : "");
            row.setUsedAt(v.getUsedAt() != null ? v.getUsedAt().toString() : "");
            row.setExpireAt(v.getExpireAt() != null ? v.getExpireAt().toString() : "");
            rows.add(row);
        }

        EasyExcel.write(response.getOutputStream(), ExportRow.class)
            .sheet("卡券数据")
            .doWrite(rows);
    }

    @Data
    public static class ExportRow {
        @ExcelProperty("券码")
        private String voucherCode;
        @ExcelProperty("持有人")
        private String holderName;
        @ExcelProperty("状态")
        private String status;
        @ExcelProperty("发放时间")
        private String issuedAt;
        @ExcelProperty("核销时间")
        private String usedAt;
        @ExcelProperty("过期时间")
        private String expireAt;
    }
}
