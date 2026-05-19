package com.example.voucher.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateBatchRequest {

    @NotBlank(message = "批次名称不能为空")
    private String batchName;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private String resourceDesc;

    @NotNull(message = "有效天数不能为空")
    @Min(value = 1, message = "有效天数至少为1天")
    private Integer validDays;

    private String discountType;

    @DecimalMin(value = "0.01", message = "折扣金额必须大于0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.01", message = "最低消费必须大于0")
    private BigDecimal minOrderAmount;

    @DecimalMin(value = "0.01", message = "面额上限必须大于0")
    private BigDecimal faceValue;

    @DecimalMin(value = "0.00", message = "赠送金额不能为负")
    private BigDecimal bonusValue;

    private Integer transferable;
}
