package com.example.voucher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GiftVoucherRequest {

    @NotNull(message = "卡券ID不能为空")
    private Long voucherId;

    @NotBlank(message = "接收人工号不能为空")
    private String toEmployeeId;

    private String message;
}
