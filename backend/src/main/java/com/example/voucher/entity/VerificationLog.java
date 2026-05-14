package com.example.voucher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("verification_log")
public class VerificationLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long voucherId;

    private String voucherCode;

    private String holderId;

    private String holderName;

    private String operatorId;

    private String operatorName;

    private LocalDateTime verifiedAt;

    private String remark;

    private LocalDateTime createdAt;
}
