package com.example.voucher.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class VoucherUsageRecord {

    private Long id;

    private Long voucherId;

    private String voucherCode;

    /** VERIFICATION / CONSUMPTION */
    private String type;

    private BigDecimal amount;

    private String operatorName;

    private String remark;

    private LocalDateTime createdAt;
}
