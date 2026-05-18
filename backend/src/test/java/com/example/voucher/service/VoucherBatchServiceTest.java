package com.example.voucher.service;

import com.example.voucher.common.BusinessException;
import com.example.voucher.dto.CreateBatchRequest;
import com.example.voucher.entity.VoucherBatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VoucherBatchServiceTest {

    @Autowired
    private VoucherBatchService batchService;

    @Test
    void create_shouldCreateResourceUsageBatch() {
        CreateBatchRequest request = new CreateBatchRequest();
        request.setBatchName("测试资源批次");
        request.setVoucherType("RESOURCE_USAGE");
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
        request.setVoucherType("COUPON");
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
    void create_shouldThrowForInvalidVoucherType() {
        CreateBatchRequest request = new CreateBatchRequest();
        request.setBatchName("无效");
        request.setVoucherType("INVALID");
        request.setValidDays(30);

        assertThrows(BusinessException.class, () -> batchService.create(request, "admin"));
    }

    @Test
    void create_shouldThrowForCouponWithoutDiscountConfig() {
        CreateBatchRequest request = new CreateBatchRequest();
        request.setBatchName("无效优惠券");
        request.setVoucherType("COUPON");
        request.setValidDays(30);
        // Missing discountType and discountValue

        assertThrows(BusinessException.class, () -> batchService.create(request, "admin"));
    }

    @Test
    void create_shouldThrowForPercentageCouponWithoutFaceValue() {
        CreateBatchRequest request = new CreateBatchRequest();
        request.setBatchName("无效折扣");
        request.setVoucherType("COUPON");
        request.setDiscountType("PERCENTAGE");
        request.setDiscountValue(new BigDecimal("15"));
        request.setValidDays(30);
        // Missing faceValue

        assertThrows(BusinessException.class, () -> batchService.create(request, "admin"));
    }

    @Test
    void getById_shouldReturnBatch() {
        CreateBatchRequest request = new CreateBatchRequest();
        request.setBatchName("查询测试");
        request.setVoucherType("RESOURCE_USAGE");
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
