package com.example.voucher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
