package com.example.voucher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("voucher_consumption")
public class VoucherConsumption {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long voucherId;

    private String voucherCode;

    private BigDecimal consumeAmount;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private BigDecimal orderAmount;

    private String operatorId;

    private String operatorName;

    private String remark;

    private LocalDateTime createdAt;
}
