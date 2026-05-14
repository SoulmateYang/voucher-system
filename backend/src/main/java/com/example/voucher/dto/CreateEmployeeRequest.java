package com.example.voucher.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateEmployeeRequest {

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotBlank(message = "工号不能为空")
    private String username;

    private String mobile;

    private String department;
}
