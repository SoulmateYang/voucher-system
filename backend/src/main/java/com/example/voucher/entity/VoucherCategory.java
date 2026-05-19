package com.example.voucher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("voucher_category")
public class VoucherCategory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private Integer sortOrder;

    private String voucherType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
