package com.example.voucher.service;

import com.example.voucher.common.BusinessException;
import com.example.voucher.dto.CreateBatchRequest;
import com.example.voucher.dto.IssueVoucherRequest;
import com.example.voucher.entity.Voucher;
import com.example.voucher.entity.VoucherBatch;
import com.example.voucher.entity.VoucherCategory;
import com.example.voucher.mapper.VoucherBatchMapper;
import com.example.voucher.mapper.VoucherCategoryMapper;
import com.example.voucher.mapper.VoucherMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VoucherServiceTest {

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private VoucherBatchService batchService;

    @Autowired
    private VoucherMapper voucherMapper;

    @Autowired
    private VoucherBatchMapper batchMapper;

    @Autowired
    private VoucherCategoryMapper categoryMapper;

    private VoucherBatch resourceBatch;
    private VoucherBatch couponBatch;

    @BeforeEach
    void setUp() {
        // Clean test data
        voucherMapper.delete(null);
        batchMapper.delete(null);

        // Create resource batch
        CreateBatchRequest rbReq = new CreateBatchRequest();
        rbReq.setBatchName("test-resource");
        rbReq.setVoucherType("RESOURCE_USAGE");
        rbReq.setValidDays(30);
        resourceBatch = batchService.create(rbReq, "admin");

        // Create coupon batch
        CreateBatchRequest cbReq = new CreateBatchRequest();
        cbReq.setBatchName("test-coupon");
        cbReq.setVoucherType("COUPON");
        cbReq.setDiscountType("FIXED_AMOUNT");
        cbReq.setDiscountValue(new BigDecimal("50"));
        cbReq.setMinOrderAmount(new BigDecimal("200"));
        cbReq.setValidDays(30);
        couponBatch = batchService.create(cbReq, "admin");

        // Issue vouchers
        IssueVoucherRequest issueReq = new IssueVoucherRequest();
        issueReq.setBatchId(resourceBatch.getId());
        issueReq.setEmployees(List.of(
            emp("E001", "张三"),
            emp("E002", "李四")
        ));
        batchService.issue(issueReq, "op1", "操作员");

        issueReq.setBatchId(couponBatch.getId());
        batchService.issue(issueReq, "op1", "操作员");
    }

    private IssueVoucherRequest.EmployeeInfo emp(String id, String name) {
        IssueVoucherRequest.EmployeeInfo e = new IssueVoucherRequest.EmployeeInfo();
        e.setEmployeeId(id);
        e.setEmployeeName(name);
        return e;
    }

    @Test
    void lookup_shouldReturnVoucherInfo() {
        Voucher first = voucherMapper.selectList(null).get(0);
        var result = voucherService.lookup(first.getVoucherCode());
        assertEquals(first.getVoucherCode(), result.get("voucherCode"));
        assertEquals("valid", result.get("status"));
    }

    @Test
    void lookup_shouldThrowForNonExistentCode() {
        BusinessException ex = assertThrows(BusinessException.class,
            () -> voucherService.lookup("NONEXISTENT"));
        assertEquals(4004, ex.getCode());
    }

    @Test
    void verify_shouldVerifyVoucher() {
        Voucher v = voucherMapper.selectList(null).get(0);
        var result = voucherService.verify(v.getVoucherCode(), "op1", "操作员", null);
        assertEquals("used", result.get("status"));
        assertNotNull(result.get("verifiedAt"));
    }

    @Test
    void verify_shouldThrowForAlreadyUsedVoucher() {
        Voucher v = voucherMapper.selectList(null).get(0);
        voucherService.verify(v.getVoucherCode(), "op1", "操作员", null);

        BusinessException ex = assertThrows(BusinessException.class,
            () -> voucherService.verify(v.getVoucherCode(), "op1", "操作员", null));
        assertEquals(4001, ex.getCode());
    }

    @Test
    void verify_shouldVerifyFixedAmountCouponWithoutOrderAmount() {
        List<Voucher> coupons = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getBatchId, couponBatch.getId()));
        Voucher coupon = coupons.get(0);

        // FIXED_AMOUNT coupons don't require orderAmount
        var result = voucherService.verify(coupon.getVoucherCode(), "op1", "操作员", null);
        assertEquals("used", result.get("status"));
    }

    @Test
    void verify_shouldThrowForCouponBelowMinOrder() {
        List<Voucher> coupons = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getBatchId, couponBatch.getId()));
        Voucher coupon = coupons.get(0);

        BusinessException ex = assertThrows(BusinessException.class,
            () -> voucherService.verify(coupon.getVoucherCode(), "op1", "操作员", new BigDecimal("100")));
        assertTrue(ex.getMessage().contains("最低消费"));
    }

    @Test
    void verify_shouldAcceptCouponWithValidOrderAmount() {
        List<Voucher> coupons = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getBatchId, couponBatch.getId()));
        Voucher coupon = coupons.get(0);

        var result = voucherService.verify(coupon.getVoucherCode(), "op1", "操作员", new BigDecimal("300"));
        assertEquals("used", result.get("status"));
        assertEquals(0, new BigDecimal("50").compareTo((BigDecimal) result.get("discountAmount")));
    }

    @Test
    void cancel_shouldCancelVoucher() {
        Voucher v = voucherMapper.selectList(null).get(0);
        voucherService.cancel(v.getId(), "op1", "操作员");

        Voucher cancelled = voucherMapper.selectById(v.getId());
        assertEquals("CANCELLED", cancelled.getStatus());
    }

    @Test
    void cancel_shouldThrowForUsedVoucher() {
        Voucher v = voucherMapper.selectList(null).get(0);
        voucherService.verify(v.getVoucherCode(), "op1", "操作员", null);

        BusinessException ex = assertThrows(BusinessException.class,
            () -> voucherService.cancel(v.getId(), "op1", "操作员"));
        assertTrue(ex.getMessage().contains("已核销"));
    }

    @Test
    void getById_shouldReturnVoucher() {
        Voucher first = voucherMapper.selectList(null).get(0);
        Voucher found = voucherService.getById(first.getId());
        assertNotNull(found);
        assertEquals(first.getVoucherCode(), found.getVoucherCode());
    }

    @Test
    void getDetailById_shouldIncludeBatchInfo() {
        Voucher first = voucherMapper.selectList(null).get(0);
        var detail = voucherService.getDetailById(first.getId());
        assertNotNull(detail.get("voucherType"));
    }

    @Test
    void getDetailById_shouldReturnNullForNonExistent() {
        assertNull(voucherService.getDetailById(99999L));
    }

    @Test
    void listByBatchId_shouldReturnVouchersInBatch() {
        var vouchers = voucherService.listByBatchId(resourceBatch.getId());
        assertEquals(2, vouchers.size());
    }

    @Test
    void toggleFavorite_shouldToggleFlag() {
        Voucher v = voucherMapper.selectList(null).get(0);
        voucherService.toggleFavorite(v.getId(), v.getHolderId());
        Voucher toggled = voucherMapper.selectById(v.getId());
        assertEquals(1, toggled.getIsFavorite());

        voucherService.toggleFavorite(v.getId(), v.getHolderId());
        toggled = voucherMapper.selectById(v.getId());
        assertEquals(0, toggled.getIsFavorite());
    }

    @Test
    void toggleFavorite_shouldThrowForWrongHolder() {
        Voucher v = voucherMapper.selectList(null).get(0);
        assertThrows(BusinessException.class,
            () -> voucherService.toggleFavorite(v.getId(), "WRONG_ID"));
    }

    @Test
    void togglePin_shouldSetPinnedAt() {
        Voucher v = voucherMapper.selectList(null).get(0);
        voucherService.togglePin(v.getId(), v.getHolderId());
        Voucher pinned = voucherMapper.selectById(v.getId());
        assertEquals(1, pinned.getIsPinned());
        assertNotNull(pinned.getPinnedAt());
    }

    @Test
    void listAllPaged_shouldReturnPagedResults() {
        var page = voucherService.listAllPaged(1, 5, null, null, null, null, null, null, null);
        assertEquals(4, page.getTotal());
    }

    @Test
    void listAllPaged_shouldFilterByHolderName() {
        var page = voucherService.listAllPaged(1, 10, null, "张三", null, null, null, null, null);
        assertEquals(2, page.getTotal());
    }

    @Test
    void listAllPaged_shouldFilterByVoucherType() {
        var page = voucherService.listAllPaged(1, 10, null, null, null, "COUPON", null, null, null);
        assertEquals(2, page.getTotal());
        page.getRecords().forEach(r -> assertEquals("COUPON", r.getVoucherType()));
    }

    @Test
    void lookup_shouldAutoExpireIfPastExpireAt() {
        Voucher v = voucherMapper.selectList(null).get(0);
        // Manually set expireAt to past
        v.setExpireAt(LocalDateTime.now().minusDays(1));
        voucherMapper.updateById(v);

        BusinessException ex = assertThrows(BusinessException.class,
            () -> voucherService.lookup(v.getVoucherCode()));
        assertEquals(4002, ex.getCode());
    }

    @Test
    void lookup_shouldThrowForCancelledVoucher() {
        Voucher v = voucherMapper.selectList(null).get(0);
        voucherService.cancel(v.getId(), "op1", "操作员");

        BusinessException ex = assertThrows(BusinessException.class,
            () -> voucherService.lookup(v.getVoucherCode()));
        assertEquals(4003, ex.getCode());
    }

    // ---- Edit voucher tests ----

    @Test
    void updateVoucher_shouldUpdateExpireAtAndRemark() {
        Voucher v = voucherMapper.selectList(null).get(0);
        LocalDateTime newExpire = LocalDateTime.now().plusDays(60);
        Voucher updated = voucherService.updateVoucher(v.getId(), newExpire, "新备注");

        assertEquals("新备注", updated.getRemark());
        // Refresh from DB
        Voucher refreshed = voucherMapper.selectById(v.getId());
        assertEquals("新备注", refreshed.getRemark());
    }

    @Test
    void updateVoucher_shouldThrowForNonExistent() {
        assertThrows(BusinessException.class,
            () -> voucherService.updateVoucher(99999L, null, "test"));
    }

    // ---- Category assignment tests ----

    @Test
    void assignCategory_shouldAssignCategoryToVoucher() {
        VoucherCategory cat = new VoucherCategory();
        cat.setName("测试分类");
        cat.setSortOrder(1);
        categoryMapper.insert(cat);

        Voucher v = voucherMapper.selectList(null).get(0);
        voucherService.assignCategory(v.getId(), cat.getId());

        Voucher refreshed = voucherMapper.selectById(v.getId());
        assertEquals(cat.getId(), refreshed.getCategoryId());
    }

    @Test
    void assignCategory_shouldThrowForNonExistentVoucher() {
        assertThrows(BusinessException.class,
            () -> voucherService.assignCategory(99999L, 1L));
    }

    @Test
    void assignCategory_shouldThrowForNonExistentCategory() {
        Voucher v = voucherMapper.selectList(null).get(0);
        assertThrows(BusinessException.class,
            () -> voucherService.assignCategory(v.getId(), 99999L));
    }

    @Test
    void batchAssignCategory_shouldUpdateMultipleVouchers() {
        VoucherCategory cat = new VoucherCategory();
        cat.setName("批量分类");
        cat.setSortOrder(1);
        categoryMapper.insert(cat);

        List<Voucher> all = voucherMapper.selectList(null);
        List<Long> ids = all.stream().map(Voucher::getId).toList();

        int affected = voucherService.batchAssignCategory(ids, cat.getId());
        assertEquals(all.size(), affected);

        for (Long id : ids) {
            Voucher v = voucherMapper.selectById(id);
            assertEquals(cat.getId(), v.getCategoryId());
        }
    }

    @Test
    void batchAssignCategory_shouldThrowWhenExceedingLimit() {
        java.util.List<Long> hugeList = new java.util.ArrayList<>();
        for (long i = 1; i <= 501; i++) {
            hugeList.add(i);
        }
        assertThrows(BusinessException.class,
            () -> voucherService.batchAssignCategory(hugeList, 1L));
    }
}
