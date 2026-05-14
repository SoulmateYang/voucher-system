package com.example.voucher.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.voucher.common.BusinessException;
import com.example.voucher.common.VoucherCodeUtil;
import com.example.voucher.dto.IssueVoucherRequest;
import com.example.voucher.entity.AuditLog;
import com.example.voucher.entity.VerificationLog;
import com.example.voucher.entity.Voucher;
import com.example.voucher.entity.VoucherBatch;
import com.example.voucher.mapper.AuditLogMapper;
import com.example.voucher.mapper.VerificationLogMapper;
import com.example.voucher.mapper.VoucherMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Map<String, Object> result = new HashMap<>();
        result.put("voucherCode", voucher.getVoucherCode());
        result.put("holderName", voucher.getHolderName());
        result.put("resourceType", "因私使用");
        result.put("validity", voucher.getExpireAt().toString());
        result.put("status", "valid");
        return result;
    }

    @Transactional
    public Map<String, Object> verify(String voucherCode, String operatorId, String operatorName) {
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
        result.put("resourceType", "因私使用");
        result.put("verifiedAt", log.getVerifiedAt().toString());
        result.put("status", "used");
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
        voucher.setIssuedAt(LocalDateTime.now());
        voucher.setExpireAt(LocalDate.now().plusDays(batch.getValidDays()).atTime(LocalTime.MAX));

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

    public List<Voucher> listByHolder(String holderId) {
        return voucherMapper.selectByHolder(holderId);
    }

    public Map<String, Object> listByHolderPaged(String holderId, int page, int size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Voucher> p =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Voucher> result =
            voucherMapper.selectPage(p,
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Voucher>()
                    .eq(Voucher::getHolderId, holderId)
                    .orderByDesc(Voucher::getCreatedAt)
            );
        Map<String, Object> res = new HashMap<>();
        res.put("records", result.getRecords());
        res.put("total", result.getTotal());
        res.put("page", page);
        res.put("size", size);
        return res;
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
}
