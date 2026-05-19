package com.example.voucher.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.voucher.common.BusinessException;
import com.example.voucher.entity.VoucherCategory;
import com.example.voucher.mapper.VoucherCategoryMapper;
import com.example.voucher.mapper.VoucherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherCategoryService {

    private final VoucherCategoryMapper categoryMapper;
    private final VoucherMapper voucherMapper;

    public List<VoucherCategory> list() {
        return categoryMapper.selectList(
            new LambdaQueryWrapper<VoucherCategory>()
                .orderByAsc(VoucherCategory::getSortOrder)
        );
    }

    public VoucherCategory create(String name) {
        LambdaQueryWrapper<VoucherCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VoucherCategory::getName, name);
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("分类名称已存在");
        }
        VoucherCategory category = new VoucherCategory();
        category.setName(name);
        long maxOrder = categoryMapper.selectList(
            new LambdaQueryWrapper<VoucherCategory>()
                .orderByDesc(VoucherCategory::getSortOrder)
                .last("LIMIT 1")
        ).stream()
            .mapToInt(VoucherCategory::getSortOrder)
            .findFirst()
            .orElse(0);
        category.setSortOrder((int) maxOrder + 1);
        categoryMapper.insert(category);
        return category;
    }

    public VoucherCategory update(Long id, String name) {
        VoucherCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        LambdaQueryWrapper<VoucherCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VoucherCategory::getName, name).ne(VoucherCategory::getId, id);
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("分类名称已存在");
        }
        category.setName(name);
        categoryMapper.updateById(category);
        return category;
    }

    @Transactional
    public void delete(Long id) {
        VoucherCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        voucherMapper.clearCategoryByCategoryId(id);
        categoryMapper.deleteById(id);
    }
}
