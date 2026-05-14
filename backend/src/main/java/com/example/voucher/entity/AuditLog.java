package com.example.voucher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("audit_log")
public class AuditLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String entityType;

    private Long entityId;

    private String action;

    private String operatorId;

    private String operatorName;

    private String detail;

    private LocalDateTime createdAt;
}
