package com.example.voucher.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class IssueVoucherRequest {

    @NotNull(message = "批次ID不能为空")
    private Long batchId;

    @NotEmpty(message = "员工列表不能为空")
    private List<EmployeeInfo> employees;

    @Data
    public static class EmployeeInfo {
        private String employeeId;
        private String employeeName;
    }
}
