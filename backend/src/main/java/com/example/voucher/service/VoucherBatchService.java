package com.example.voucher.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.voucher.common.BusinessException;
import com.example.voucher.common.VoucherType;
import com.example.voucher.dto.CreateBatchRequest;
import com.example.voucher.dto.IssueVoucherRequest;
import com.example.voucher.entity.VoucherBatch;
import com.example.voucher.mapper.VoucherBatchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class VoucherBatchService {

    private final VoucherBatchMapper batchMapper;
    private final VoucherService voucherService;

    public VoucherBatch create(CreateBatchRequest request, String operatorName) {
        if (!VoucherType.isValid(request.getVoucherType())) {
            throw new BusinessException("无效的券类型: " + request.getVoucherType());
        }
        if (VoucherType.isCoupon(request.getVoucherType())) {
            if (request.getDiscountType() == null || request.getDiscountValue() == null) {
                throw new BusinessException("优惠券必须配置折扣类型和折扣值");
            }
            VoucherType.validateDiscountType(request.getDiscountType());
            if ("PERCENTAGE".equals(request.getDiscountType()) && request.getFaceValue() == null) {
                throw new BusinessException("折扣率优惠券必须设置面额上限");
            }
        }

        VoucherBatch batch = new VoucherBatch();
        batch.setBatchName(request.getBatchName());
        batch.setVoucherType(request.getVoucherType());
        batch.setResourceDesc(request.getResourceDesc());
        batch.setValidDays(request.getValidDays());
        batch.setTotalCount(0);
        batch.setStatus("ACTIVE");
        batch.setCreatedBy(operatorName);
        batch.setDiscountType(request.getDiscountType());
        batch.setDiscountValue(request.getDiscountValue());
        batch.setMinOrderAmount(request.getMinOrderAmount());
        batch.setFaceValue(request.getFaceValue());
        batchMapper.insert(batch);
        return batch;
    }

    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> issue(IssueVoucherRequest request, String operatorId, String operatorName) {
        VoucherBatch batch = batchMapper.selectById(request.getBatchId());
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }
        if (!"ACTIVE".equals(batch.getStatus())) {
            throw new BusinessException("批次状态不允许发券");
        }
        Map<String, Object> result = voucherService.issueBatch(batch, request.getEmployees(), operatorId, operatorName);
        int successCount = (int) result.get("successCount");
        batchMapper.update(null,
            Wrappers.<VoucherBatch>lambdaUpdate()
                .eq(VoucherBatch::getId, batch.getId())
                .setSql("total_count = total_count + " + successCount)
        );
        return result;
    }

    public VoucherBatch getById(Long id) {
        return batchMapper.selectById(id);
    }

    public java.util.List<VoucherBatch> list() {
        return batchMapper.selectList(
            Wrappers.<VoucherBatch>lambdaQuery().orderByDesc(VoucherBatch::getCreatedAt)
        );
    }

    public java.util.List<com.example.voucher.entity.Voucher> getVouchersByBatchId(Long batchId) {
        return voucherService.listByBatchId(batchId);
    }
}
