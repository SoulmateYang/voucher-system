package com.example.voucher.service;

import com.example.voucher.common.BusinessException;
import com.example.voucher.dto.CreateBatchRequest;
import com.example.voucher.entity.VoucherBatch;
import com.example.voucher.entity.VoucherCategory;
import com.example.voucher.mapper.VoucherCategoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VoucherBatchServiceTest {

    @Autowired
    private VoucherBatchService batchService;

    @Autowired
    private VoucherCategoryMapper categoryMapper;

    private Long couponCategoryId;
    private Long resourceCategoryId;
    private Long storedValueCategoryId;

    @BeforeEach
    void setUp() {
        categoryMapper.delete(null);
        VoucherCategory couponCat = new VoucherCategory();
        couponCat.setName("优惠券分类");
        couponCat.setVoucherType("COUPON");
        couponCat.setSortOrder(1);
        categoryMapper.insert(couponCat);
        couponCategoryId = couponCat.getId();

        VoucherCategory resourceCat = new VoucherCategory();
        resourceCat.setName("因私使用分类");
        resourceCat.setVoucherType("RESOURCE_USAGE");
        resourceCat.setSortOrder(2);
        categoryMapper.insert(resourceCat);
        resourceCategoryId = resourceCat.getId();

        VoucherCategory storedCat = new VoucherCategory();
        storedCat.setName("储值卡分类");
        storedCat.setVoucherType("STORED_VALUE");
        storedCat.setSortOrder(3);
        categoryMapper.insert(storedCat);
        storedValueCategoryId = storedCat.getId();
    }

    @Test
    void create_shouldCreateResourceUsageBatch() {
        CreateBatchRequest request = new CreateBatchRequest();
        request.setBatchName("测试资源批次");
        request.setCategoryId(resourceCategoryId);
        request.setResourceDesc("会议室使用权限");
        request.setValidDays(30);

        VoucherBatch batch = batchService.create(request, "admin");
        assertNotNull(batch.getId());
        assertEquals("测试资源批次", batch.getBatchName());
        assertEquals("RESOURCE_USAGE", batch.getVoucherType());
        assertEquals("ACTIVE", batch.getStatus());
    }

    @Test
    void create_shouldCreateCouponBatch() {
        CreateBatchRequest request = new CreateBatchRequest();
        request.setBatchName("测试优惠券批次");
        request.setCategoryId(couponCategoryId);
        request.setDiscountType("FIXED_AMOUNT");
        request.setDiscountValue(new BigDecimal("50.00"));
        request.setMinOrderAmount(new BigDecimal("200.00"));
        request.setValidDays(30);

        VoucherBatch batch = batchService.create(request, "admin");
        assertNotNull(batch.getId());
        assertEquals("COUPON", batch.getVoucherType());
        assertEquals("FIXED_AMOUNT", batch.getDiscountType());
    }

    @Test
    void create_shouldCreateStoredValueBatch() {
        CreateBatchRequest request = new CreateBatchRequest();
        request.setBatchName("测试储值卡批次");
        request.setCategoryId(storedValueCategoryId);
        request.setFaceValue(new BigDecimal("50.00"));
        request.setBonusValue(new BigDecimal("5.00"));
        request.setValidDays(365);

        VoucherBatch batch = batchService.create(request, "admin");
        assertNotNull(batch.getId());
        assertEquals("STORED_VALUE", batch.getVoucherType());
        assertEquals(0, new BigDecimal("50.00").compareTo(batch.getFaceValue()));
        assertEquals(0, new BigDecimal("5.00").compareTo(batch.getBonusValue()));
    }

    @Test
    void create_shouldThrowForStoredValueWithoutFaceValue() {
        CreateBatchRequest request = new CreateBatchRequest();
        request.setBatchName("无效储值卡");
        request.setCategoryId(storedValueCategoryId);
        request.setValidDays(30);

        assertThrows(BusinessException.class, () -> batchService.create(request, "admin"));
    }

    @Test
    void create_shouldThrowForNonExistentCategory() {
        CreateBatchRequest request = new CreateBatchRequest();
        request.setBatchName("无效");
        request.setCategoryId(99999L);
        request.setValidDays(30);

        assertThrows(BusinessException.class, () -> batchService.create(request, "admin"));
    }

    @Test
    void create_shouldThrowForCouponWithoutDiscountConfig() {
        CreateBatchRequest request = new CreateBatchRequest();
        request.setBatchName("无效优惠券");
        request.setCategoryId(couponCategoryId);
        request.setValidDays(30);

        assertThrows(BusinessException.class, () -> batchService.create(request, "admin"));
    }

    @Test
    void create_shouldThrowForPercentageCouponWithoutFaceValue() {
        CreateBatchRequest request = new CreateBatchRequest();
        request.setBatchName("无效折扣");
        request.setCategoryId(couponCategoryId);
        request.setDiscountType("PERCENTAGE");
        request.setDiscountValue(new BigDecimal("15"));
        request.setValidDays(30);

        assertThrows(BusinessException.class, () -> batchService.create(request, "admin"));
    }

    @Test
    void getById_shouldReturnBatch() {
        CreateBatchRequest request = new CreateBatchRequest();
        request.setBatchName("查询测试");
        request.setCategoryId(resourceCategoryId);
        request.setValidDays(30);
        VoucherBatch created = batchService.create(request, "admin");

        VoucherBatch found = batchService.getById(created.getId());
        assertNotNull(found);
        assertEquals("查询测试", found.getBatchName());
    }

    @Test
    void list_shouldReturnAllBatches() {
        var batches = batchService.list();
        assertNotNull(batches);
    }

    @Test
    void issue_shouldThrowForNonExistentBatch() {
        var request = new com.example.voucher.dto.IssueVoucherRequest();
        request.setBatchId(99999L);

        assertThrows(BusinessException.class, () -> batchService.issue(request, "op1", "operator"));
    }
}
