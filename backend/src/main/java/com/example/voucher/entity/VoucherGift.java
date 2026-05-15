package com.example.voucher.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("voucher_gift")
public class VoucherGift {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long voucherId;

    private String fromUserId;

    private String fromUserName;

    private String toUserId;

    private String toUserName;

    private String message;

    private String status;

    private LocalDateTime giftAt;

    private LocalDateTime handledAt;

    private LocalDateTime expireAt;

    private LocalDateTime createdAt;
}
