package com.example.voucher.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBatchRequest {

    @NotBlank(message = "批次名称不能为空")
    private String batchName;

    private String resourceDesc;

    @NotNull(message = "有效天数不能为空")
    @Min(value = 1, message = "有效天数至少为1天")
    private Integer validDays;
}
