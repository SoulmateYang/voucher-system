package com.example.voucher.controller;

import com.example.voucher.common.Result;
import com.example.voucher.dto.CreateEmployeeRequest;
import com.example.voucher.dto.UpdateEmployeeRequest;
import com.example.voucher.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public Result<Object> list(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int pageSize,
                               @RequestParam(required = false) String keyword) {
        return Result.success(employeeService.list(page, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public Result<Object> getById(@PathVariable Long id) {
        return Result.success(employeeService.getById(id));
    }

    @PostMapping
    public Result<Object> create(@Valid @RequestBody CreateEmployeeRequest request) {
        return Result.success(employeeService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Object> update(@PathVariable Long id,
                                 @Valid @RequestBody UpdateEmployeeRequest request) {
        employeeService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Object> delete(@PathVariable Long id, Authentication auth) {
        employeeService.delete(id, auth.getName());
        return Result.success();
    }

    @PostMapping("/{id}/reset-password")
    public Result<Object> resetPassword(@PathVariable Long id) {
        return Result.success(employeeService.resetPassword(id));
    }
}
