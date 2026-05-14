package com.example.voucher.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateEmployeeRequest {

    @NotBlank(message = "姓名不能为空")
    private String realName;

    private String mobile;

    private String department;
}
