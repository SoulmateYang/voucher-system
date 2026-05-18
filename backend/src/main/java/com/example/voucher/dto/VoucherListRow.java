package com.example.voucher.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VoucherListRow {

    private Long id;
    private Long batchId;
    private String voucherCode;
    private String holderId;
    private String holderName;
    private String status;
    private LocalDateTime issuedAt;
    private LocalDateTime expireAt;
    private LocalDateTime usedAt;
    private String approveRef;
    private String remark;
    private BigDecimal faceValue;
    private String batchName;
    private String voucherType;
}
