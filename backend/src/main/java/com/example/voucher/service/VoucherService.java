package com.example.voucher.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.voucher.common.BusinessException;
import com.example.voucher.common.VoucherCodeUtil;
import com.example.voucher.dto.IssueVoucherRequest;
import com.example.voucher.dto.VoucherListRow;
import com.example.voucher.entity.AuditLog;
import com.example.voucher.entity.VerificationLog;
import com.example.voucher.entity.Voucher;
import com.example.voucher.entity.VoucherBatch;
import com.example.voucher.entity.VoucherCategory;
import com.example.voucher.mapper.AuditLogMapper;
import com.example.voucher.mapper.VerificationLogMapper;
import com.example.voucher.mapper.VoucherBatchMapper;
import com.example.voucher.mapper.VoucherCategoryMapper;
import com.example.voucher.mapper.VoucherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherMapper voucherMapper;
    private final VerificationLogMapper verificationLogMapper;
    private final AuditLogMapper auditLogMapper;
    private final VoucherBatchMapper batchMapper;
    private final VoucherCategoryMapper categoryMapper;
    private final VoucherCodeUtil voucherCodeUtil;

    public Map<String, Object> lookup(String voucherCode) {
        Voucher voucher = voucherMapper.selectByCode(voucherCode);
        if (voucher == null) {
            throw new BusinessException(4004, "券不存在");
        }
        if ("EXPIRED".equals(voucher.getStatus())) {
            throw new BusinessException(4002, "该券已过期");
        }
        if ("CANCELLED".equals(voucher.getStatus())) {
            throw new BusinessException(4003, "该券已作废");
        }
        if (LocalDateTime.now().isAfter(voucher.getExpireAt())) {
            voucher.setStatus("EXPIRED");
            voucherMapper.updateById(voucher);
            writeAudit("voucher", voucher.getId(), "EXPIRE", "SYSTEM", "系统",
                Map.of("voucherCode", voucher.getVoucherCode(), "reason", "lookup-triggered")
            );
            throw new BusinessException(4002, "该券已过期");
        }
        if ("USED".equals(voucher.getStatus())) {
            throw new BusinessException(4001, "该券已被核销");
        }

        VoucherBatch batch = null;
        if (voucher.getBatchId() != null) {
            batch = batchMapper.selectById(voucher.getBatchId());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("voucherCode", voucher.getVoucherCode());
        result.put("holderName", voucher.getHolderName());
        result.put("resourceType", "因私使用");
        result.put("validity", voucher.getExpireAt().toString());
        result.put("status", "valid");
        result.put("faceValue", voucher.getFaceValue());
        if (batch != null) {
            result.put("voucherType", batch.getVoucherType());
            if ("COUPON".equals(batch.getVoucherType())) {
                result.put("discountType", batch.getDiscountType());
                result.put("discountValue", batch.getDiscountValue());
                result.put("minOrderAmount", batch.getMinOrderAmount());
            }
        } else {
            result.put("voucherType", "RESOURCE_USAGE");
        }
        return result;
    }

    @Transactional
    public Map<String, Object> verify(String voucherCode, String operatorId, String operatorName,
                                       BigDecimal orderAmount) {
        Voucher voucher = voucherMapper.selectByCode(voucherCode);
        if (voucher == null) {
            throw new BusinessException(4004, "券不存在");
        }
        if ("USED".equals(voucher.getStatus())) {
            throw new BusinessException(4001, "该券已被核销");
        }
        if ("EXPIRED".equals(voucher.getStatus())) {
            throw new BusinessException(4002, "该券已过期");
        }
        if ("CANCELLED".equals(voucher.getStatus())) {
            throw new BusinessException(4003, "该券已作废");
        }
        if (LocalDateTime.now().isAfter(voucher.getExpireAt())) {
            voucher.setStatus("EXPIRED");
            voucherMapper.updateById(voucher);
            writeAudit("voucher", voucher.getId(), "EXPIRE", operatorId, operatorName,
                Map.of("voucherCode", voucher.getVoucherCode(), "reason", "verify-triggered")
            );
            throw new BusinessException(4002, "该券已过期");
        }

        VoucherBatch batch = null;
        if (voucher.getBatchId() != null) {
            batch = batchMapper.selectById(voucher.getBatchId());
        }
        String voucherType = batch != null ? batch.getVoucherType() : "RESOURCE_USAGE";

        // Coupon-specific validation (only when batch exists)
        if (batch != null && "COUPON".equals(voucherType)) {
            if ("PERCENTAGE".equals(batch.getDiscountType()) && orderAmount == null) {
                throw new BusinessException(4005, "请输入订单金额");
            }
            if (orderAmount != null && batch.getMinOrderAmount() != null
                && orderAmount.compareTo(batch.getMinOrderAmount()) < 0) {
                throw new BusinessException(4005,
                    String.format("订单金额未达到使用门槛（最低消费 %.2f 元）", batch.getMinOrderAmount()));
            }
        }

        voucher.setStatus("USED");
        voucher.setUsedAt(LocalDateTime.now());
        int updated = voucherMapper.update(voucher,
            new LambdaUpdateWrapper<Voucher>()
                .eq(Voucher::getId, voucher.getId())
                .eq(Voucher::getVersion, voucher.getVersion())
        );
        if (updated == 0) {
            throw new BusinessException(4001, "该券已被核销");
        }

        VerificationLog log = new VerificationLog();
        log.setVoucherId(voucher.getId());
        log.setVoucherCode(voucher.getVoucherCode());
        log.setHolderId(voucher.getHolderId());
        log.setHolderName(voucher.getHolderName());
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setVerifiedAt(LocalDateTime.now());
        verificationLogMapper.insert(log);

        writeAudit("voucher", voucher.getId(), "VERIFY", operatorId, operatorName,
            Map.of("voucherCode", voucher.getVoucherCode(), "holderName", voucher.getHolderName())
        );

        Map<String, Object> result = new HashMap<>();
        result.put("voucherCode", voucher.getVoucherCode());
        result.put("holderName", voucher.getHolderName());
        result.put("voucherType", voucherType);
        result.put("status", "used");
        result.put("verifiedAt", log.getVerifiedAt().toString());

        BigDecimal discountAmount = BigDecimal.ZERO;
        if ("COUPON".equals(voucherType) && voucher.getFaceValue() != null) {
            if ("FIXED_AMOUNT".equals(batch.getDiscountType())) {
                discountAmount = voucher.getFaceValue();
            } else if (orderAmount != null) {
                BigDecimal rate = batch.getDiscountValue()
                    .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                BigDecimal raw = orderAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
                if (raw.compareTo(voucher.getFaceValue()) > 0) {
                    result.put("cappedByFaceValue", true);
                    discountAmount = voucher.getFaceValue();
                } else {
                    result.put("cappedByFaceValue", false);
                    discountAmount = raw;
                }
            }
        }
        result.put("discountAmount", discountAmount);
        return result;
    }

    public List<Map<String, Object>> getTodayRecords(String operatorId) {
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.now().with(LocalTime.MAX);
        List<VerificationLog> logs = verificationLogMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VerificationLog>()
                .eq(VerificationLog::getOperatorId, operatorId)
                .between(VerificationLog::getVerifiedAt, todayStart, todayEnd)
                .orderByDesc(VerificationLog::getVerifiedAt)
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (VerificationLog log : logs) {
            Map<String, Object> item = new HashMap<>();
            item.put("voucherCode", log.getVoucherCode());
            item.put("holderName", log.getHolderName());
            item.put("verifiedAt", log.getVerifiedAt().toString());
            result.add(item);
        }
        return result;
    }

    @Transactional
    public void cancel(Long voucherId, String operatorId, String operatorName) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null) {
            throw new BusinessException("券不存在");
        }
        if ("USED".equals(voucher.getStatus())) {
            throw new BusinessException("不能作废已核销的券");
        }
        voucher.setStatus("CANCELLED");
        voucher.setCancelledAt(LocalDateTime.now());
        voucherMapper.updateById(voucher);

        writeAudit("voucher", voucher.getId(), "CANCEL", operatorId, operatorName,
            Map.of("voucherCode", voucher.getVoucherCode())
        );
    }

    @Transactional
    public Map<String, Object> issueBatch(VoucherBatch batch,
                                           List<IssueVoucherRequest.EmployeeInfo> employees,
                                           String operatorId, String operatorName) {
        List<Map<String, Object>> failList = new ArrayList<>();
        int successCount = 0;

        for (IssueVoucherRequest.EmployeeInfo emp : employees) {
            try {
                issueSingle(batch, emp.getEmployeeId(), emp.getEmployeeName());
                successCount++;
            } catch (BusinessException | DuplicateKeyException e) {
                Map<String, Object> fail = new HashMap<>();
                fail.put("employeeId", emp.getEmployeeId());
                fail.put("reason", e.getMessage());
                failList.add(fail);
            }
        }

        writeAudit("batch", batch.getId(), "ISSUE", operatorId, operatorName,
            Map.of("successCount", successCount, "failCount", failList.size())
        );

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failList", failList);
        return result;
    }

    private Voucher issueSingle(VoucherBatch batch, String employeeId, String employeeName) {
        Voucher voucher = new Voucher();
        voucher.setBatchId(batch.getId());
        voucher.setHolderId(employeeId);
        voucher.setHolderName(employeeName);
        voucher.setStatus("ISSUED");
        voucher.setVersion(0);
        voucher.setTransferable(batch.getTransferable());
        voucher.setIssuedAt(LocalDateTime.now());
        voucher.setExpireAt(LocalDate.now().plusDays(batch.getValidDays()).atTime(LocalTime.MAX));

        if ("COUPON".equals(batch.getVoucherType())) {
            if ("FIXED_AMOUNT".equals(batch.getDiscountType())) {
                voucher.setFaceValue(batch.getDiscountValue());
            } else {
                voucher.setFaceValue(batch.getFaceValue());
            }
        }

        for (int i = 0; i < 3; i++) {
            voucher.setVoucherCode(voucherCodeUtil.generate());
            try {
                voucherMapper.insert(voucher);
                return voucher;
            } catch (DuplicateKeyException e) {
                if (i == 2) throw new BusinessException("券码生成冲突，请重试");
            }
        }
        throw new BusinessException("券码生成冲突");
    }

    public Voucher getById(Long id) {
        return voucherMapper.selectById(id);
    }

    public Map<String, Object> getDetailById(Long id) {
        Voucher voucher = voucherMapper.selectById(id);
        if (voucher == null) return null;

        VoucherBatch batch = batchMapper.selectById(voucher.getBatchId());
        Map<String, Object> result = new HashMap<>();
        result.put("id", voucher.getId());
        result.put("batchId", voucher.getBatchId());
        result.put("voucherCode", voucher.getVoucherCode());
        result.put("holderId", voucher.getHolderId());
        result.put("holderName", voucher.getHolderName());
        result.put("status", voucher.getStatus());
        result.put("issuedAt", voucher.getIssuedAt());
        result.put("expireAt", voucher.getExpireAt());
        result.put("usedAt", voucher.getUsedAt());
        result.put("cancelledAt", voucher.getCancelledAt());
        result.put("approveRef", voucher.getApproveRef());
        result.put("remark", voucher.getRemark());
        result.put("faceValue", voucher.getFaceValue());
        result.put("transferable", voucher.getTransferable());
        result.put("isFavorite", voucher.getIsFavorite());
        result.put("isPinned", voucher.getIsPinned());
        if (batch != null) {
            result.put("voucherType", batch.getVoucherType());
            result.put("discountType", batch.getDiscountType());
            result.put("discountValue", batch.getDiscountValue());
            result.put("minOrderAmount", batch.getMinOrderAmount());
        }
        return result;
    }

    public List<Map<String, Object>> listByHolder(String holderId) {
        List<Voucher> vouchers = voucherMapper.selectByHolder(holderId);
        Map<Long, String> batchTypeMap = getBatchTypeMap(vouchers);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Voucher v : vouchers) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", v.getId());
            item.put("batchId", v.getBatchId());
            item.put("voucherCode", v.getVoucherCode());
            item.put("holderId", v.getHolderId());
            item.put("holderName", v.getHolderName());
            item.put("status", v.getStatus());
            item.put("issuedAt", v.getIssuedAt());
            item.put("expireAt", v.getExpireAt());
            item.put("usedAt", v.getUsedAt());
            item.put("cancelledAt", v.getCancelledAt());
            item.put("approveRef", v.getApproveRef());
            item.put("remark", v.getRemark());
            item.put("faceValue", v.getFaceValue());
            item.put("voucherType", batchTypeMap.get(v.getBatchId()));
            result.add(item);
        }
        return result;
    }

    public Map<String, Object> listByHolderPaged(String holderId, int page, int size,
                                                  String keyword, String status, String voucherType) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Voucher> p =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Voucher::getHolderId, holderId);

        // 排除赠送中的卡券
        wrapper.ne(Voucher::getStatus, "GIFTING");

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Voucher::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Voucher::getRemark, keyword);
        }

        // 排序：置顶优先 → 收藏优先 → 过期时间升序
        wrapper.orderByDesc(Voucher::getIsPinned)
               .orderByDesc(Voucher::getIsFavorite)
               .orderByAsc(Voucher::getExpireAt);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Voucher> pageResult =
            voucherMapper.selectPage(p, wrapper);

        List<Voucher> vouchers = pageResult.getRecords();
        Map<Long, String> batchTypeMap = getBatchTypeMap(vouchers);
        List<Map<String, Object>> records = new ArrayList<>();
        for (Voucher v : vouchers) {
            String vt = batchTypeMap.get(v.getBatchId());
            if (voucherType != null && !voucherType.isEmpty() && !voucherType.equals(vt)) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("id", v.getId());
            item.put("batchId", v.getBatchId());
            item.put("voucherCode", v.getVoucherCode());
            item.put("holderId", v.getHolderId());
            item.put("holderName", v.getHolderName());
            item.put("status", v.getStatus());
            item.put("issuedAt", v.getIssuedAt());
            item.put("expireAt", v.getExpireAt());
            item.put("usedAt", v.getUsedAt());
            item.put("cancelledAt", v.getCancelledAt());
            item.put("approveRef", v.getApproveRef());
            item.put("remark", v.getRemark());
            item.put("faceValue", v.getFaceValue());
            item.put("isFavorite", v.getIsFavorite());
            item.put("isPinned", v.getIsPinned());
            item.put("source", v.getSource());
            item.put("transferable", v.getTransferable());
            item.put("voucherType", vt);
            records.add(item);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("records", records);
        res.put("total", pageResult.getTotal());
        res.put("page", page);
        res.put("size", size);
        return res;
    }

    private Map<Long, String> getBatchTypeMap(List<Voucher> vouchers) {
        Set<Long> batchIds = vouchers.stream()
            .map(Voucher::getBatchId)
            .collect(java.util.stream.Collectors.toSet());
        if (batchIds.isEmpty()) {
            return Map.of();
        }
        List<VoucherBatch> batches = batchMapper.selectBatchIds(batchIds);
        Map<Long, String> map = new HashMap<>();
        for (VoucherBatch b : batches) {
            map.put(b.getId(), b.getVoucherType());
        }
        return map;
    }

    public List<Voucher> listByBatchId(Long batchId) {
        return voucherMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getBatchId, batchId)
                .orderByDesc(Voucher::getCreatedAt)
        );
    }

    private void writeAudit(String entityType, Long entityId, String action,
                            String operatorId, String operatorName, Object detail) {
        AuditLog auditLog = AuditLog.builder()
            .entityType(entityType)
            .entityId(entityId)
            .action(action)
            .operatorId(operatorId)
            .operatorName(operatorName)
            .detail(JSONUtil.toJsonStr(detail))
            .createdAt(LocalDateTime.now())
            .build();
        auditLogMapper.insert(auditLog);
    }

    public void toggleFavorite(Long voucherId, String holderId) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null || !voucher.getHolderId().equals(holderId)) {
            throw new BusinessException("卡券不存在或无权操作");
        }
        voucher.setIsFavorite(voucher.getIsFavorite() != null && voucher.getIsFavorite() == 1 ? 0 : 1);
        voucherMapper.updateById(voucher);
    }

    public void togglePin(Long voucherId, String holderId) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null || !voucher.getHolderId().equals(holderId)) {
            throw new BusinessException("卡券不存在或无权操作");
        }
        if (voucher.getIsPinned() != null && voucher.getIsPinned() == 1) {
            voucher.setIsPinned(0);
            voucher.setPinnedAt(null);
        } else {
            voucher.setIsPinned(1);
            voucher.setPinnedAt(LocalDateTime.now());
        }
        voucherMapper.updateById(voucher);
    }

    @Transactional
    public Map<String, Object> addManual(Voucher voucher, String holderId, String holderName) {
        voucher.setHolderId(holderId);
        voucher.setHolderName(holderName);
        voucher.setStatus("ISSUED");
        voucher.setVersion(0);
        voucher.setSource("MANUAL");
        voucher.setTransferable(1);
        voucher.setIssuedAt(LocalDateTime.now());
        voucher.setCreatedAt(LocalDateTime.now());
        voucher.setUpdatedAt(LocalDateTime.now());

        for (int i = 0; i < 3; i++) {
            voucher.setVoucherCode(voucherCodeUtil.generate());
            try {
                voucherMapper.insert(voucher);
                break;
            } catch (DuplicateKeyException e) {
                if (i == 2) throw new BusinessException("券码生成冲突，请重试");
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", voucher.getId());
        result.put("voucherCode", voucher.getVoucherCode());
        return result;
    }

    public IPage<VoucherListRow> listAllPaged(int page, int size,
                                               String holderId, String holderName,
                                               String keyword, String voucherType,
                                               LocalDateTime expireStart, LocalDateTime expireEnd,
                                               Long categoryId) {
        Page<VoucherListRow> p = new Page<>(page, size);
        LocalDateTime expireEndAdjusted = expireEnd;
        if (expireEnd != null) {
            expireEndAdjusted = expireEnd.withHour(23).withMinute(59).withSecond(59);
        }
        return voucherMapper.selectPagedWithBatch(p, holderId, holderName, keyword,
            voucherType, expireStart, expireEndAdjusted, categoryId);
    }

    @Transactional
    public Voucher updateVoucher(Long id, LocalDateTime expireAt, String remark) {
        Voucher voucher = voucherMapper.selectById(id);
        if (voucher == null) {
            throw new BusinessException("券不存在");
        }
        Map<String, Object> changes = new HashMap<>();
        if (expireAt != null) {
            Map<String, Object> expireChange = new HashMap<>();
            expireChange.put("old", voucher.getExpireAt() != null ? voucher.getExpireAt().toString() : null);
            expireChange.put("new", expireAt.toString());
            changes.put("expireAt", expireChange);
            voucher.setExpireAt(expireAt);
        }
        if (remark != null) {
            Map<String, Object> remarkChange = new HashMap<>();
            remarkChange.put("old", voucher.getRemark());
            remarkChange.put("new", remark);
            changes.put("remark", remarkChange);
            voucher.setRemark(remark);
        }
        if (changes.isEmpty()) {
            return voucher;
        }
        voucher.setUpdatedAt(LocalDateTime.now());
        voucherMapper.updateById(voucher);
        writeAudit("voucher", voucher.getId(), "EDIT_VOUCHER", null, null, changes);
        return voucher;
    }

    public void assignCategory(Long voucherId, Long categoryId) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null) {
            throw new BusinessException("券不存在");
        }
        if (categoryId != null) {
            VoucherCategory category = categoryMapper.selectById(categoryId);
            if (category == null) {
                throw new BusinessException("分类不存在");
            }
        }
        voucher.setCategoryId(categoryId);
        voucher.setUpdatedAt(LocalDateTime.now());
        voucherMapper.updateById(voucher);
    }

    public int batchAssignCategory(List<Long> voucherIds, Long categoryId) {
        if (voucherIds == null || voucherIds.isEmpty()) {
            throw new BusinessException("请选择要操作的券");
        }
        if (voucherIds.size() > 500) {
            throw new BusinessException("单次最多操作 500 条");
        }
        if (categoryId != null) {
            VoucherCategory category = categoryMapper.selectById(categoryId);
            if (category == null) {
                throw new BusinessException("分类不存在");
            }
        }
        return voucherMapper.batchUpdateCategory(voucherIds, categoryId);
    }
}
