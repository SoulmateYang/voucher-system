package com.example.voucher.controller;

import com.example.voucher.common.Result;
import com.example.voucher.common.VoucherType;
import com.example.voucher.service.VoucherCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final VoucherCategoryService categoryService;

    @GetMapping
    public Result<Object> list() {
        return Result.success(categoryService.list());
    }

    @PostMapping
    public Result<Object> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String voucherType = body.get("voucherType");
        if (name == null || name.isBlank()) {
            return Result.error("分类名称不能为空");
        }
        if (voucherType == null || voucherType.isBlank()) {
            return Result.error("卷类型不能为空");
        }
        if (!VoucherType.isValid(voucherType)) {
            return Result.error("无效的卷类型");
        }
        return Result.success(categoryService.create(name.trim(), voucherType));
    }

    @PutMapping("/{id}")
    public Result<Object> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        String voucherType = body.get("voucherType");
        if (name == null || name.isBlank()) {
            return Result.error("分类名称不能为空");
        }
        if (voucherType == null || voucherType.isBlank()) {
            return Result.error("卷类型不能为空");
        }
        if (!VoucherType.isValid(voucherType)) {
            return Result.error("无效的卷类型");
        }
        return Result.success(categoryService.update(id, name.trim(), voucherType));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
