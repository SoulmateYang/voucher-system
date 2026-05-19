package com.example.voucher.service;

import com.example.voucher.common.BusinessException;
import com.example.voucher.entity.VoucherCategory;
import com.example.voucher.mapper.VoucherCategoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VoucherCategoryServiceTest {

    @Autowired
    private VoucherCategoryService categoryService;

    @Autowired
    private VoucherCategoryMapper categoryMapper;

    @BeforeEach
    void setUp() {
        categoryMapper.delete(null);
    }

    @Test
    void testCreateCategory() {
        VoucherCategory cat = categoryService.create("测试分类", "COUPON");
        assertNotNull(cat.getId());
        assertEquals("测试分类", cat.getName());
        assertEquals("COUPON", cat.getVoucherType());
    }

    @Test
    void testCreateCategoryWithResourceUsageType() {
        VoucherCategory cat = categoryService.create("因公", "RESOURCE_USAGE");
        assertEquals("RESOURCE_USAGE", cat.getVoucherType());
    }

    @Test
    void testCreateCategoryWithStoredValueType() {
        VoucherCategory cat = categoryService.create("储值", "STORED_VALUE");
        assertEquals("STORED_VALUE", cat.getVoucherType());
    }

    @Test
    void testCreateDuplicateNameThrowsException() {
        categoryService.create("唯一分类", "COUPON");
        assertThrows(BusinessException.class, () -> categoryService.create("唯一分类", "COUPON"));
    }

    @Test
    void testListCategories() {
        categoryService.create("分类A", "COUPON");
        categoryService.create("分类B", "RESOURCE_USAGE");
        List<VoucherCategory> list = categoryService.list();
        assertEquals(2, list.size());
        assertTrue(list.get(0).getSortOrder() <= list.get(1).getSortOrder());
    }

    @Test
    void testUpdateCategory() {
        VoucherCategory cat = categoryService.create("旧名称", "COUPON");
        VoucherCategory updated = categoryService.update(cat.getId(), "新名称", "STORED_VALUE");
        assertEquals("新名称", updated.getName());
        assertEquals("STORED_VALUE", updated.getVoucherType());
    }

    @Test
    void testDeleteCategory() {
        VoucherCategory cat = categoryService.create("待删除", "COUPON");
        categoryService.delete(cat.getId());
        assertNull(categoryMapper.selectById(cat.getId()));
    }

    @Test
    void testDeleteNonExistentCategoryThrowsException() {
        assertThrows(BusinessException.class, () -> categoryService.delete(99999L));
    }
}
