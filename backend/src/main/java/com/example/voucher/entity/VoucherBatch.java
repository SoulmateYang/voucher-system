package com.example.voucher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("voucher_batch")
public class VoucherBatch {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String batchName;

    private String voucherType;

    private String resourceDesc;

    private Integer totalCount;

    private Integer validDays;

    private String status;

    private String createdBy;

    private Long categoryId;

    private String discountType;

    private BigDecimal discountValue;

    private BigDecimal minOrderAmount;

    private BigDecimal faceValue;

    private BigDecimal bonusValue;

    private Integer transferable;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
