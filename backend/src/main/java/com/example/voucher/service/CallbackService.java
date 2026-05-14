package com.example.voucher.service;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.voucher.common.BusinessException;
import com.example.voucher.dto.IssueVoucherRequest;
import com.example.voucher.entity.VoucherBatch;
import com.example.voucher.mapper.VoucherBatchMapper;
import com.example.voucher.mapper.VoucherMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackService {

    @Resource
    private VoucherMapper voucherMapper;
    private final VoucherBatchMapper batchMapper;
    private final VoucherService voucherService;


    @Value("${app.callback.shared-secret}")
    private String sharedSecret;

    public Map<String, Object> handleApproval(String payload, String signature) {
        String expected = new HMac(HmacAlgorithm.HmacSHA256, sharedSecret.getBytes())
            .digestHex(payload);
        if (!expected.equals(signature)) {
            throw new BusinessException(4006, "回调签名验证失败");
        }

        JSONObject json = JSONUtil.parseObj(payload);
        String approveId = json.getStr("approveId");
        String employeeId = json.getStr("employeeId");
        String employeeName = json.getStr("employeeName", "");
        String resourceDesc = json.getStr("resourceDesc", "因私使用");
        Integer validDays = json.getInt("validDays", 30);

        boolean exists = voucherMapper.exists(
            Wrappers.lambdaQuery(com.example.voucher.entity.Voucher.class)
                .eq(com.example.voucher.entity.Voucher::getApproveRef, approveId)
        );
        if (exists) {
            throw new BusinessException(4005, "重复回调");
        }

        VoucherBatch batch = new VoucherBatch();
        batch.setBatchName("OA审批-" + approveId);
        batch.setResourceDesc(resourceDesc);
        batch.setValidDays(validDays);
        batch.setTotalCount(0);
        batch.setStatus("ACTIVE");
        batch.setCreatedBy("OA_CALLBACK");
        batchMapper.insert(batch);

        IssueVoucherRequest.EmployeeInfo emp = new IssueVoucherRequest.EmployeeInfo();
        emp.setEmployeeId(employeeId);
        emp.setEmployeeName(employeeName);

        Map<String, Object> result = voucherService.issueBatch(batch, List.of(emp),
            "OA_CALLBACK", "OA审批系统");
        log.info("OA callback issued voucher: approveId={}, employeeId={}", approveId, employeeId);
        return result;
    }
}
