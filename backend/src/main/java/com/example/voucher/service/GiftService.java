package com.example.voucher.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.voucher.common.BusinessException;
import com.example.voucher.entity.Voucher;
import com.example.voucher.entity.VoucherGift;
import com.example.voucher.entity.SysUser;
import com.example.voucher.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GiftService {

    private final VoucherGiftMapper giftMapper;
    private final VoucherMapper voucherMapper;
    private final VoucherBatchMapper batchMapper;
    private final SysUserMapper sysUserMapper;

    /** 赠送卡券 */
    @Transactional
    public Map<String, Object> gift(Long voucherId, String fromUserId, String fromUserName,
                                     String toEmployeeId, String message) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null) {
            throw new BusinessException("卡券不存在");
        }
        if (!voucher.getHolderId().equals(fromUserId)) {
            throw new BusinessException("无权操作该卡券");
        }
        if ("USED".equals(voucher.getStatus())) {
            throw new BusinessException("已使用的卡券不可赠送");
        }
        if ("EXPIRED".equals(voucher.getStatus())) {
            throw new BusinessException("已过期的卡券不可赠送");
        }
        if ("CANCELLED".equals(voucher.getStatus())) {
            throw new BusinessException("已作废的卡券不可赠送");
        }
        if ("GIFTING".equals(voucher.getStatus())) {
            throw new BusinessException("该卡券正在赠送中");
        }
        if (voucher.getTransferable() != null && voucher.getTransferable() == 0) {
            throw new BusinessException("该卡券不可转赠");
        }

        // 查找接收人
        SysUser toUser = sysUserMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, toEmployeeId)
                .eq(SysUser::getEnabled, 1)
        );
        if (toUser == null) {
            throw new BusinessException("接收人不存在");
        }
        if (toUser.getUsername().equals(fromUserId)) {
            throw new BusinessException("不可赠送给自己");
        }

        // 更新卡券状态
        voucher.setStatus("GIFTING");
        voucherMapper.updateById(voucher);

        // 创建赠送记录
        VoucherGift gift = new VoucherGift();
        gift.setVoucherId(voucher.getId());
        gift.setFromUserId(fromUserId);
        gift.setFromUserName(fromUserName);
        gift.setToUserId(toUser.getUsername());
        gift.setToUserName(toUser.getRealName());
        gift.setMessage(message);
        gift.setStatus("PENDING");
        gift.setGiftAt(LocalDateTime.now());
        gift.setExpireAt(LocalDateTime.now().plusHours(24));
        giftMapper.insert(gift);

        Map<String, Object> result = new HashMap<>();
        result.put("giftId", gift.getId());
        result.put("expireAt", gift.getExpireAt().toString());
        return result;
    }

    /** 撤销赠送 */
    @Transactional
    public void cancelGift(Long giftId, String userId) {
        VoucherGift gift = giftMapper.selectById(giftId);
        if (gift == null) {
            throw new BusinessException("赠送记录不存在");
        }
        if (!gift.getFromUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        if (!"PENDING".equals(gift.getStatus())) {
            throw new BusinessException("该赠送已处理，无法撤销");
        }
        if (LocalDateTime.now().isAfter(gift.getExpireAt())) {
            gift.setStatus("EXPIRED");
            gift.setHandledAt(LocalDateTime.now());
            giftMapper.updateById(gift);
            restoreVoucher(gift.getVoucherId());
            throw new BusinessException("该赠送已超时");
        }

        gift.setStatus("CANCELLED");
        gift.setHandledAt(LocalDateTime.now());
        giftMapper.updateById(gift);

        restoreVoucher(gift.getVoucherId());
    }

    /** 接收赠送 */
    @Transactional
    public void acceptGift(Long giftId, String userId) {
        VoucherGift gift = giftMapper.selectById(giftId);
        if (gift == null) {
            throw new BusinessException("赠送记录不存在");
        }
        if (!gift.getToUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        if (!"PENDING".equals(gift.getStatus())) {
            throw new BusinessException("该赠送已处理");
        }
        if (LocalDateTime.now().isAfter(gift.getExpireAt())) {
            gift.setStatus("EXPIRED");
            gift.setHandledAt(LocalDateTime.now());
            giftMapper.updateById(gift);
            restoreVoucher(gift.getVoucherId());
            throw new BusinessException("该赠送已超时");
        }

        // 转移卡券归属
        Voucher voucher = voucherMapper.selectById(gift.getVoucherId());
        voucher.setHolderId(gift.getToUserId());
        voucher.setHolderName(gift.getToUserName());
        voucher.setStatus("ISSUED");
        voucherMapper.updateById(voucher);

        gift.setStatus("ACCEPTED");
        gift.setHandledAt(LocalDateTime.now());
        giftMapper.updateById(gift);
    }

    /** 拒绝赠送 */
    @Transactional
    public void rejectGift(Long giftId, String userId) {
        VoucherGift gift = giftMapper.selectById(giftId);
        if (gift == null) {
            throw new BusinessException("赠送记录不存在");
        }
        if (!gift.getToUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        if (!"PENDING".equals(gift.getStatus())) {
            throw new BusinessException("该赠送已处理");
        }
        if (LocalDateTime.now().isAfter(gift.getExpireAt())) {
            gift.setStatus("EXPIRED");
            gift.setHandledAt(LocalDateTime.now());
            giftMapper.updateById(gift);
            restoreVoucher(gift.getVoucherId());
            throw new BusinessException("该赠送已超时");
        }

        gift.setStatus("REJECTED");
        gift.setHandledAt(LocalDateTime.now());
        giftMapper.updateById(gift);

        restoreVoucher(gift.getVoucherId());
    }

    /** 查询收件箱（含懒校验超时） */
    public List<Map<String, Object>> getInbox(String userId) {
        processExpiredGifts(userId);

        List<VoucherGift> gifts = giftMapper.selectList(
            new LambdaQueryWrapper<VoucherGift>()
                .eq(VoucherGift::getToUserId, userId)
                .orderByDesc(VoucherGift::getGiftAt)
        );
        return buildGiftResultList(gifts);
    }

    /** 查询发件箱（含懒校验超时） */
    public List<Map<String, Object>> getOutbox(String userId) {
        List<VoucherGift> expiredGifts = giftMapper.selectList(
            new LambdaQueryWrapper<VoucherGift>()
                .eq(VoucherGift::getFromUserId, userId)
                .eq(VoucherGift::getStatus, "PENDING")
                .lt(VoucherGift::getExpireAt, LocalDateTime.now())
        );
        for (VoucherGift g : expiredGifts) {
            g.setStatus("EXPIRED");
            g.setHandledAt(LocalDateTime.now());
            giftMapper.updateById(g);
            restoreVoucher(g.getVoucherId());
        }

        List<VoucherGift> gifts = giftMapper.selectList(
            new LambdaQueryWrapper<VoucherGift>()
                .eq(VoucherGift::getFromUserId, userId)
                .orderByDesc(VoucherGift::getGiftAt)
        );
        return buildGiftResultList(gifts);
    }

    private void processExpiredGifts(String userId) {
        List<VoucherGift> expiredGifts = giftMapper.selectList(
            new LambdaQueryWrapper<VoucherGift>()
                .eq(VoucherGift::getToUserId, userId)
                .eq(VoucherGift::getStatus, "PENDING")
                .lt(VoucherGift::getExpireAt, LocalDateTime.now())
        );
        for (VoucherGift g : expiredGifts) {
            g.setStatus("EXPIRED");
            g.setHandledAt(LocalDateTime.now());
            giftMapper.updateById(g);
            restoreVoucher(g.getVoucherId());
        }
    }

    private void restoreVoucher(Long voucherId) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher != null && "GIFTING".equals(voucher.getStatus())) {
            VoucherGift gift = giftMapper.selectOne(
                new LambdaQueryWrapper<VoucherGift>()
                    .eq(VoucherGift::getVoucherId, voucherId)
                    .orderByDesc(VoucherGift::getGiftAt)
                    .last("LIMIT 1")
            );
            voucher.setHolderId(gift.getFromUserId());
            voucher.setHolderName(gift.getFromUserName());
            voucher.setStatus("ISSUED");
            voucherMapper.updateById(voucher);
        }
    }

    private List<Map<String, Object>> buildGiftResultList(List<VoucherGift> gifts) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (VoucherGift g : gifts) {
            Voucher voucher = voucherMapper.selectById(g.getVoucherId());
            Map<String, Object> item = new HashMap<>();
            item.put("giftId", g.getId());
            item.put("voucherId", g.getVoucherId());
            item.put("fromUserId", g.getFromUserId());
            item.put("fromUserName", g.getFromUserName());
            item.put("toUserId", g.getToUserId());
            item.put("toUserName", g.getToUserName());
            item.put("message", g.getMessage());
            item.put("status", g.getStatus());
            item.put("giftAt", g.getGiftAt().toString());
            item.put("expireAt", g.getExpireAt().toString());
            if (g.getHandledAt() != null) {
                item.put("handledAt", g.getHandledAt().toString());
            }
            if (voucher != null) {
                item.put("voucherCode", voucher.getVoucherCode());
                item.put("faceValue", voucher.getFaceValue());
                item.put("remark", voucher.getRemark());
                item.put("expireAt", voucher.getExpireAt() != null ? voucher.getExpireAt().toString() : null);
            }
            result.add(item);
        }
        return result;
    }
}
