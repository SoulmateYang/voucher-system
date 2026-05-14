package com.example.voucher.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    private String username;

    private String employeeId;

    @NotBlank(message = "密码不能为空")
    private String password;

    public String getUsername() {
        return username != null ? username : employeeId;
    }
}
