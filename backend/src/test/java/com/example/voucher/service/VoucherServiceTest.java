package com.example.voucher.service;

import com.example.voucher.common.BusinessException;
import com.example.voucher.dto.CreateBatchRequest;
import com.example.voucher.dto.IssueVoucherRequest;
import com.example.voucher.entity.Voucher;
import com.example.voucher.entity.VoucherBatch;
import com.example.voucher.entity.VoucherCategory;
import com.example.voucher.mapper.VoucherBatchMapper;
import com.example.voucher.mapper.VoucherCategoryMapper;
import com.example.voucher.mapper.VoucherConsumptionMapper;
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

    @Autowired
    private VoucherConsumptionMapper consumptionMapper;

    private VoucherBatch resourceBatch;
    private VoucherBatch couponBatch;
    private VoucherBatch storedValueBatch;
    private VoucherCategory couponCategory;
    private VoucherCategory resourceCategory;
    private VoucherCategory storedValueCategory;
    private Long nextEmpId = 1L;

    @BeforeEach
    void setUp() {
        // Clean test data
        voucherMapper.delete(null);
        batchMapper.delete(null);
        categoryMapper.delete(null);

        // Create categories
        couponCategory = new VoucherCategory();
        couponCategory.setName("优惠券分类");
        couponCategory.setVoucherType("COUPON");
        couponCategory.setSortOrder(1);
        categoryMapper.insert(couponCategory);

        resourceCategory = new VoucherCategory();
        resourceCategory.setName("因私使用分类");
        resourceCategory.setVoucherType("RESOURCE_USAGE");
        resourceCategory.setSortOrder(2);
        categoryMapper.insert(resourceCategory);

        storedValueCategory = new VoucherCategory();
        storedValueCategory.setName("储值卡分类");
        storedValueCategory.setVoucherType("STORED_VALUE");
        storedValueCategory.setSortOrder(3);
        categoryMapper.insert(storedValueCategory);

        // Create resource batch
        CreateBatchRequest rbReq = new CreateBatchRequest();
        rbReq.setBatchName("test-resource");
        rbReq.setCategoryId(resourceCategory.getId());
        rbReq.setValidDays(30);
        resourceBatch = batchService.create(rbReq, "admin");

        // Create coupon batch
        CreateBatchRequest cbReq = new CreateBatchRequest();
        cbReq.setBatchName("test-coupon");
        cbReq.setCategoryId(couponCategory.getId());
        cbReq.setDiscountType("FIXED_AMOUNT");
        cbReq.setDiscountValue(new BigDecimal("50"));
        cbReq.setMinOrderAmount(new BigDecimal("200"));
        cbReq.setValidDays(30);
        couponBatch = batchService.create(cbReq, "admin");

        // Create stored-value batch (charge 50, bonus 5 = total 55)
        CreateBatchRequest svReq = new CreateBatchRequest();
        svReq.setBatchName("test-stored-value");
        svReq.setCategoryId(storedValueCategory.getId());
        svReq.setFaceValue(new BigDecimal("50"));
        svReq.setBonusValue(new BigDecimal("5"));
        svReq.setValidDays(365);
        storedValueBatch = batchService.create(svReq, "admin");

        // Issue vouchers
        issueTo(resourceBatch, "E001", "张三");
        issueTo(resourceBatch, "E002", "李四");
        issueTo(couponBatch, "E001", "张三");
        issueTo(couponBatch, "E002", "李四");
        issueTo(storedValueBatch, "E003", "王五");
        issueTo(storedValueBatch, "E004", "赵六");
    }

    private void issueTo(VoucherBatch batch, String empId, String empName) {
        IssueVoucherRequest issueReq = new IssueVoucherRequest();
        issueReq.setBatchId(batch.getId());
        issueReq.setEmployees(List.of(emp(empId, empName)));
        batchService.issue(issueReq, "op1", "操作员");
    }

    private IssueVoucherRequest.EmployeeInfo emp(String id, String name) {
        IssueVoucherRequest.EmployeeInfo e = new IssueVoucherRequest.EmployeeInfo();
        e.setEmployeeId(id);
        e.setEmployeeName(name);
        return e;
    }

    // ---- Existing tests ----

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
        assertEquals(6, page.getTotal());
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
        // Get a COUPON type voucher and assign to coupon category
        Voucher v = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getBatchId, couponBatch.getId())).get(0);
        voucherService.assignCategory(v.getId(), couponCategory.getId());

        Voucher refreshed = voucherMapper.selectById(v.getId());
        assertEquals(couponCategory.getId(), refreshed.getCategoryId());
    }

    @Test
    void assignCategory_shouldThrowForTypeMismatch() {
        Voucher v = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getBatchId, resourceBatch.getId())).get(0);
        // Try to assign RESOURCE_USAGE voucher to COUPON category
        assertThrows(BusinessException.class,
            () -> voucherService.assignCategory(v.getId(), couponCategory.getId()));
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
    void batchAssignCategory_shouldThrowForTypeMismatch() {
        VoucherCategory couponCat = couponCategory;
        List<Voucher> all = voucherMapper.selectList(null);
        List<Long> ids = all.stream().map(Voucher::getId).toList();

        assertThrows(BusinessException.class,
            () -> voucherService.batchAssignCategory(ids, couponCat.getId()));
    }

    @Test
    void batchAssignCategory_shouldUpdateMultipleVouchers() {
        // Use all resource-type vouchers with resource category
        List<Voucher> resourceVouchers = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getBatchId, resourceBatch.getId()));
        List<Long> ids = resourceVouchers.stream().map(Voucher::getId).toList();

        int affected = voucherService.batchAssignCategory(ids, resourceCategory.getId());
        assertEquals(resourceVouchers.size(), affected);

        for (Long id : ids) {
            Voucher v = voucherMapper.selectById(id);
            assertEquals(resourceCategory.getId(), v.getCategoryId());
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

    // ---- Stored-value card tests ----

    @Test
    void storedValueCard_shouldHaveCorrectBalancesAfterIssue() {
        List<Voucher> cards = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getBatchId, storedValueBatch.getId()));
        Voucher card = cards.get(0);

        assertEquals(0, new BigDecimal("55.00").compareTo(card.getFaceValue()));
        assertEquals(0, new BigDecimal("55.00").compareTo(card.getInitialBalance()));
        assertEquals(0, new BigDecimal("55.00").compareTo(card.getRemainingBalance()));
        assertEquals("ISSUED", card.getStatus());
    }

    @Test
    void storedValueCard_partialRedemption_shouldDeductBalance() {
        List<Voucher> cards = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getBatchId, storedValueBatch.getId()));
        Voucher card = cards.get(0);

        var result = voucherService.verify(card.getVoucherCode(), "op1", "操作员", new BigDecimal("23.50"));

        assertEquals(0, new BigDecimal("23.50").compareTo((BigDecimal) result.get("deductAmount")));
        assertEquals(0, new BigDecimal("31.50").compareTo((BigDecimal) result.get("remainingBalance")));
        assertEquals("ISSUED", result.get("status"));

        Voucher refreshed = voucherMapper.selectById(card.getId());
        assertEquals(0, new BigDecimal("31.50").compareTo(refreshed.getRemainingBalance()));
        assertEquals("ISSUED", refreshed.getStatus());
    }

    @Test
    void storedValueCard_multipleRedemptions_shouldTrackBalance() {
        List<Voucher> cards = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getBatchId, storedValueBatch.getId()));
        Voucher card = cards.get(0);

        voucherService.verify(card.getVoucherCode(), "op1", "操作员", new BigDecimal("20.00"));
        voucherService.verify(card.getVoucherCode(), "op1", "操作员", new BigDecimal("15.50"));

        Voucher refreshed = voucherMapper.selectById(card.getId());
        assertEquals(0, new BigDecimal("19.50").compareTo(refreshed.getRemainingBalance()));
        assertEquals("ISSUED", refreshed.getStatus());
    }

    @Test
    void storedValueCard_fullRedemption_shouldSetExhausted() {
        List<Voucher> cards = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getBatchId, storedValueBatch.getId()));
        Voucher card = cards.get(0);

        var result = voucherService.verify(card.getVoucherCode(), "op1", "操作员", new BigDecimal("55.00"));

        assertEquals("EXHAUSTED", result.get("status"));
        assertEquals(0, new BigDecimal("0").compareTo((BigDecimal) result.get("remainingBalance")));

        Voucher refreshed = voucherMapper.selectById(card.getId());
        assertEquals("EXHAUSTED", refreshed.getStatus());
    }

    @Test
    void storedValueCard_shouldThrowWhenInsufficientBalance() {
        List<Voucher> cards = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getBatchId, storedValueBatch.getId()));
        Voucher card = cards.get(0);

        BusinessException ex = assertThrows(BusinessException.class,
            () -> voucherService.verify(card.getVoucherCode(), "op1", "操作员", new BigDecimal("100.00")));
        assertTrue(ex.getMessage().contains("余额不足"));
    }

    @Test
    void storedValueCard_shouldThrowWhenNoOrderAmount() {
        List<Voucher> cards = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getBatchId, storedValueBatch.getId()));
        Voucher card = cards.get(0);

        BusinessException ex = assertThrows(BusinessException.class,
            () -> voucherService.verify(card.getVoucherCode(), "op1", "操作员", null));
        assertTrue(ex.getMessage().contains("消费金额"));
    }

    @Test
    void storedValueCard_exhaustedCardCannotBeRedeemed() {
        List<Voucher> cards = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getBatchId, storedValueBatch.getId()));
        Voucher card = cards.get(0);

        voucherService.verify(card.getVoucherCode(), "op1", "操作员", new BigDecimal("55.00"));

        BusinessException ex = assertThrows(BusinessException.class,
            () -> voucherService.verify(card.getVoucherCode(), "op1", "操作员", new BigDecimal("10.00")));
        assertTrue(ex.getMessage().contains("已用完"));
    }

    @Test
    void storedValueCard_shouldCreateConsumptionRecords() {
        List<Voucher> cards = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getBatchId, storedValueBatch.getId()));
        Voucher card = cards.get(0);

        voucherService.verify(card.getVoucherCode(), "op1", "操作员", new BigDecimal("20.00"));
        voucherService.verify(card.getVoucherCode(), "op2", "核销员2", new BigDecimal("10.00"));

        var records = consumptionMapper.selectByVoucherId(card.getId());
        assertEquals(2, records.size());
        assertEquals(0, new BigDecimal("20.00").compareTo(records.get(1).getConsumeAmount()));
        assertEquals(0, new BigDecimal("10.00").compareTo(records.get(0).getConsumeAmount()));
        assertEquals("op2", records.get(0).getOperatorId());
    }

    @Test
    void getDetailById_shouldReturnBalancesForStoredValue() {
        List<Voucher> cards = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getBatchId, storedValueBatch.getId()));
        Voucher card = cards.get(0);

        var detail = voucherService.getDetailById(card.getId());
        assertEquals("STORED_VALUE", detail.get("voucherType"));
        assertNotNull(detail.get("initialBalance"));
        assertNotNull(detail.get("remainingBalance"));
    }
}
