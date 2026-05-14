package com.example.voucher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("voucher")
public class Voucher {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long batchId;

    private String voucherCode;

    private String holderId;

    private String holderName;

    private String status;

    @Version
    private Integer version;

    private LocalDateTime issuedAt;

    private LocalDateTime expireAt;

    private LocalDateTime usedAt;

    private LocalDateTime cancelledAt;

    private String approveRef;

    private String remark;

    private BigDecimal faceValue;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
