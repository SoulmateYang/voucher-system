package com.example.voucher.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.voucher.entity.Voucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface VoucherMapper extends BaseMapper<Voucher> {

    default Voucher selectByCode(String voucherCode) {
        return selectOne(
            Wrappers.<Voucher>lambdaQuery().eq(Voucher::getVoucherCode, voucherCode)
        );
    }

    default List<Voucher> selectByHolder(String holderId) {
        return selectList(
            Wrappers.<Voucher>lambdaQuery()
                .eq(Voucher::getHolderId, holderId)
                .orderByDesc(Voucher::getCreatedAt)
        );
    }

    @Select("SELECT status, COUNT(*) as cnt FROM voucher " +
            "WHERE created_at BETWEEN #{start} AND #{end} " +
            "GROUP BY status")
    List<Map<String, Object>> countByStatus(LocalDateTime start, LocalDateTime end);

    @Select("SELECT DATE(created_at) as dt, COUNT(*) as cnt FROM voucher " +
            "WHERE created_at BETWEEN #{start} AND #{end} " +
            "GROUP BY DATE(created_at) ORDER BY dt")
    List<Map<String, Object>> dailyIssueCount(LocalDateTime start, LocalDateTime end);

    @Select("SELECT DATE(verified_at) as dt, COUNT(*) as cnt FROM verification_log " +
            "WHERE verified_at BETWEEN #{start} AND #{end} " +
            "GROUP BY DATE(verified_at) ORDER BY dt")
    List<Map<String, Object>> dailyVerifyCount(LocalDateTime start, LocalDateTime end);
}
