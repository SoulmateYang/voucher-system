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
