package com.example.voucher.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ManualVoucherRequest {

    @NotBlank(message = "卡券名称不能为空")
    private String name;

    private String voucherType;

    private BigDecimal faceValue;

    private String expireAt;

    private String voucherCode;

    private String remark;
}
