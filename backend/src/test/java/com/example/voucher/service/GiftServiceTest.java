package com.example.voucher.service;

import com.example.voucher.common.BusinessException;
import com.example.voucher.dto.CreateBatchRequest;
import com.example.voucher.dto.IssueVoucherRequest;
import com.example.voucher.entity.Voucher;
import com.example.voucher.entity.VoucherBatch;
import com.example.voucher.entity.VoucherCategory;
import com.example.voucher.entity.SysUser;
import com.example.voucher.mapper.SysUserMapper;
import com.example.voucher.mapper.VoucherBatchMapper;
import com.example.voucher.mapper.VoucherCategoryMapper;
import com.example.voucher.mapper.VoucherGiftMapper;
import com.example.voucher.mapper.VoucherMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GiftServiceTest {

    @Autowired
    private GiftService giftService;

    @Autowired
    private VoucherBatchService batchService;

    @Autowired
    private VoucherMapper voucherMapper;

    @Autowired
    private VoucherBatchMapper batchMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private VoucherGiftMapper giftMapper;

    @Autowired
    private VoucherCategoryMapper categoryMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long giftId;
    private Long resourceCategoryId;

    private void ensureUser(Long id, String username, String realName) {
        SysUser existing = userMapper.selectById(id);
        if (existing == null) {
            SysUser byName = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, username));
            if (byName != null) {
                userMapper.deleteById(byName.getId());
            }
            SysUser u = new SysUser();
            u.setId(id);
            u.setUsername(username);
            u.setRealName(realName);
            u.setPassword(passwordEncoder.encode("test123"));
            u.setRole("EMPLOYEE");
            u.setEnabled(1);
            userMapper.insert(u);
        }
    }

    @BeforeEach
    void setUp() {
        // Clean data
        giftMapper.delete(null);
        voucherMapper.delete(null);
        batchMapper.delete(null);
        categoryMapper.delete(null);

        // Create category for resource usage
        VoucherCategory cat = new VoucherCategory();
        cat.setName("因私使用分类");
        cat.setVoucherType("RESOURCE_USAGE");
        cat.setSortOrder(1);
        categoryMapper.insert(cat);
        resourceCategoryId = cat.getId();

        // Create test users
        ensureUser(10L, "E001", "张三");
        ensureUser(11L, "E002", "李四");
        ensureUser(12L, "E003", "王五");

        // Create batch and issue voucher
        CreateBatchRequest req = new CreateBatchRequest();
        req.setBatchName("gift-test");
        req.setCategoryId(resourceCategoryId);
        req.setValidDays(30);
        req.setTransferable(1);
        VoucherBatch batch = batchService.create(req, "admin");

        IssueVoucherRequest issueReq = new IssueVoucherRequest();
        issueReq.setBatchId(batch.getId());
        issueReq.setEmployees(List.of(emp("E001", "张三")));
        batchService.issue(issueReq, "op1", "操作员");

        // Gift the voucher from E001 to E002
        Voucher voucher = voucherMapper.selectList(null).get(0);
        var result = giftService.gift(voucher.getId(), "E001", "张三", "E002", "请收下");
        giftId = ((Number) result.get("giftId")).longValue();
    }

    private IssueVoucherRequest.EmployeeInfo emp(String id, String name) {
        IssueVoucherRequest.EmployeeInfo e = new IssueVoucherRequest.EmployeeInfo();
        e.setEmployeeId(id);
        e.setEmployeeName(name);
        return e;
    }

    // === Gift basic operations ===

    @Test
    void gift_shouldCreateGiftRecordAndChangeStatus() {
        assertNotNull(giftId);
        Voucher voucher = voucherMapper.selectList(null).get(0);
        assertEquals("GIFTING", voucher.getStatus());
    }

    @Test
    void gift_shouldThrowForNonTransferableVoucher() {
        CreateBatchRequest req = new CreateBatchRequest();
        req.setBatchName("non-transferable");
        req.setCategoryId(resourceCategoryId);
        req.setValidDays(30);
        req.setTransferable(0);
        VoucherBatch batch = batchService.create(req, "admin");

        IssueVoucherRequest issueReq = new IssueVoucherRequest();
        issueReq.setBatchId(batch.getId());
        issueReq.setEmployees(List.of(emp("E003", "王五")));
        batchService.issue(issueReq, "op1", "操作员");

        Voucher v = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getHolderId, "E003")).get(0);

        assertThrows(BusinessException.class,
            () -> giftService.gift(v.getId(), "E003", "王五", "E001", "test"));
    }

    @Test
    void acceptGift_shouldTransferOwnership() {
        giftService.acceptGift(giftId, "E002");

        // Voucher should now belong to E002
        Voucher voucher = voucherMapper.selectList(null).get(0);
        assertEquals("E002", voucher.getHolderId());
        assertEquals("ISSUED", voucher.getStatus());
    }

    @Test
    void rejectGift_shouldRestoreVoucher() {
        giftService.rejectGift(giftId, "E002");

        Voucher voucher = voucherMapper.selectList(null).get(0);
        assertEquals("E001", voucher.getHolderId());
        assertEquals("ISSUED", voucher.getStatus());
    }

    @Test
    void cancelGift_shouldCancelAndRestore() {
        giftService.cancelGift(giftId, "E001");

        Voucher voucher = voucherMapper.selectList(null).get(0);
        assertEquals("E001", voucher.getHolderId());
        assertEquals("ISSUED", voucher.getStatus());
    }

    // === Inbox / Outbox ===

    @Test
    void getInbox_shouldReturnReceivedGifts() {
        var inbox = giftService.getInbox("E002");
        assertFalse(inbox.isEmpty());
    }

    @Test
    void getOutbox_shouldReturnSentGifts() {
        var outbox = giftService.getOutbox("E001");
        assertFalse(outbox.isEmpty());
    }

    // === NEW: Gift records for a specific voucher ===

    @Test
    void getGiftRecordsByVoucher_shouldReturnTransferHistory() {
        // Accept the gift so we have a complete history
        giftService.acceptGift(giftId, "E002");

        // Query gift records by voucher ID
        Voucher voucher = voucherMapper.selectList(null).get(0);
        List<Map<String, Object>> records = giftService.getGiftRecordsByVoucherId(voucher.getId());

        // Should have at least one gift record
        assertFalse(records.isEmpty(), "Should return gift records for this voucher");

        // First record should have from/to details
        Map<String, Object> record = records.get(0);
        assertEquals("E001", record.get("fromUserId"));
        assertEquals("张三", record.get("fromUserName"));
        assertEquals("E002", record.get("toUserId"));
        assertEquals("ACCEPTED", record.get("status"));
        assertNotNull(record.get("giftAt"));
    }

    @Test
    void getGiftRecordsByVoucher_shouldReturnEmptyForNonGiftedVoucher() {
        // Create a voucher that was never gifted
        CreateBatchRequest req = new CreateBatchRequest();
        req.setBatchName("never-gifted");
        req.setCategoryId(resourceCategoryId);
        req.setValidDays(30);
        VoucherBatch batch = batchService.create(req, "admin");

        IssueVoucherRequest issueReq = new IssueVoucherRequest();
        issueReq.setBatchId(batch.getId());
        issueReq.setEmployees(List.of(emp("E004", "赵六")));
        batchService.issue(issueReq, "op1", "操作员");

        Voucher v = voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getHolderId, "E004")).get(0);

        List<Map<String, Object>> records = giftService.getGiftRecordsByVoucherId(v.getId());
        assertTrue(records.isEmpty(), "Never-gifted voucher should have no records");
    }

    @Test
    void getGiftRecordsByVoucher_shouldReturnMultipleRecordsForMultipleTransfers() {
        // Accept first gift
        giftService.acceptGift(giftId, "E002");

        // Gift again from E002 to E003
        Voucher voucher = voucherMapper.selectList(null).get(0);
        giftService.gift(voucher.getId(), "E002", "李四", "E003", "转赠给你");

        List<Map<String, Object>> records = giftService.getGiftRecordsByVoucherId(voucher.getId());
        assertEquals(2, records.size(), "Should have 2 transfer records");
        // Records ordered by giftAt DESC, latest first
        assertEquals("E002", records.get(0).get("fromUserId"));
        assertEquals("E001", records.get(1).get("fromUserId"));
    }

    // === processExpiredGiftingVouchers ===

    @Test
    void processExpiredGiftingVouchers_shouldRestoreExpiredGifts() {
        // Manually expire the gift
        var gifts = giftMapper.selectList(null);
        for (var g : gifts) {
            g.setExpireAt(LocalDateTime.now().minusHours(1));
            giftMapper.updateById(g);
        }

        int restored = giftService.processExpiredGiftingVouchers(500);
        assertTrue(restored > 0, "Should restore at least one expired gift");

        // Voucher should be back to ISSUED with original holder
        Voucher voucher = voucherMapper.selectList(null).get(0);
        assertEquals("ISSUED", voucher.getStatus());
        assertEquals("E001", voucher.getHolderId());
    }

    @Test
    void processExpiredGiftingVouchers_shouldNotAffectAcceptedGifts() {
        // Accept the gift first
        giftService.acceptGift(giftId, "E002");

        int restored = giftService.processExpiredGiftingVouchers(500);
        assertEquals(0, restored, "Accepted gifts should not be restored");

        // Voucher should still belong to E002
        Voucher voucher = voucherMapper.selectList(null).get(0);
        assertEquals("E002", voucher.getHolderId());
    }

    @Test
    void restoreVoucher_shouldNotThrowWhenGiftRecordMissing() {
        // Delete gift records
        giftMapper.delete(null);

        // Manually set voucher to GIFTING and restore
        Voucher voucher = voucherMapper.selectList(null).get(0);
        voucher.setStatus("GIFTING");
        voucherMapper.updateById(voucher);

        // processExpiredGiftingVouchers should not throw
        assertDoesNotThrow(() -> giftService.processExpiredGiftingVouchers(500));
    }
}
