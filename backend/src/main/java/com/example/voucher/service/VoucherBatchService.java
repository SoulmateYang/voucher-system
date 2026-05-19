package com.example.voucher.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.voucher.common.BusinessException;
import com.example.voucher.common.VoucherType;
import com.example.voucher.dto.CreateBatchRequest;
import com.example.voucher.dto.IssueVoucherRequest;
import com.example.voucher.entity.VoucherBatch;
import com.example.voucher.entity.VoucherCategory;
import com.example.voucher.mapper.VoucherBatchMapper;
import com.example.voucher.mapper.VoucherCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class VoucherBatchService {

    private final VoucherBatchMapper batchMapper;
    private final VoucherCategoryMapper categoryMapper;
    private final VoucherService voucherService;

    public VoucherBatch create(CreateBatchRequest request, String operatorName) {
        VoucherCategory category = categoryMapper.selectById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        String voucherType = category.getVoucherType();

        if (VoucherType.isCoupon(voucherType)) {
            if (request.getDiscountType() == null || request.getDiscountValue() == null) {
                throw new BusinessException("优惠券必须配置折扣类型和折扣值");
            }
            VoucherType.validateDiscountType(request.getDiscountType());
            if ("PERCENTAGE".equals(request.getDiscountType()) && request.getFaceValue() == null) {
                throw new BusinessException("折扣率优惠券必须设置面额上限");
            }
        }

        if (VoucherType.isStoredValue(voucherType)) {
            if (request.getFaceValue() == null || request.getFaceValue().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new BusinessException("储值卡必须设置充值金额");
            }
        }

        VoucherBatch batch = new VoucherBatch();
        batch.setBatchName(request.getBatchName());
        batch.setCategoryId(category.getId());
        batch.setVoucherType(voucherType);
        batch.setResourceDesc(request.getResourceDesc());
        batch.setValidDays(request.getValidDays());
        batch.setTotalCount(0);
        batch.setStatus("ACTIVE");
        batch.setCreatedBy(operatorName);
        batch.setDiscountType(request.getDiscountType());
        batch.setDiscountValue(request.getDiscountValue());
        batch.setMinOrderAmount(request.getMinOrderAmount());
        batch.setFaceValue(request.getFaceValue());
        batch.setBonusValue(request.getBonusValue());
        if (request.getTransferable() != null) {
            batch.setTransferable(request.getTransferable());
        }
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
