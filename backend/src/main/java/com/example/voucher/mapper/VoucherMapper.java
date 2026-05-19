package com.example.voucher.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.voucher.dto.VoucherListRow;
import com.example.voucher.entity.Voucher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import org.apache.ibatis.annotations.Update;

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

    @Select("SELECT DATE(expire_at) as dt, COUNT(*) as cnt FROM voucher " +
            "WHERE expire_at BETWEEN #{start} AND #{end} " +
            "GROUP BY DATE(expire_at) ORDER BY dt")
    List<Map<String, Object>> dailyExpiredCount(LocalDateTime start, LocalDateTime end);

    @Select("SELECT DATE(cancelled_at) as dt, COUNT(*) as cnt FROM voucher " +
            "WHERE cancelled_at BETWEEN #{start} AND #{end} " +
            "GROUP BY DATE(cancelled_at) ORDER BY dt")
    List<Map<String, Object>> dailyCancelledCount(LocalDateTime start, LocalDateTime end);

    @Select("<script>" +
        "SELECT v.id, v.batch_id, v.voucher_code, v.holder_id, v.holder_name, " +
        "       v.status, v.issued_at, v.expire_at, v.used_at, v.approve_ref, " +
        "       v.remark, v.face_value, v.initial_balance, v.remaining_balance, v.created_at, " +
        "       b.batch_name, b.voucher_type, " +
        "       v.category_id, c.name AS category_name " +
        "FROM voucher v " +
        "LEFT JOIN voucher_batch b ON v.batch_id = b.id " +
        "LEFT JOIN voucher_category c ON v.category_id = c.id " +
        "<where>" +
        "  <if test='holderId != null and holderId != \"\"'>AND v.holder_id LIKE CONCAT('%', #{holderId}, '%')</if>" +
        "  <if test='holderName != null and holderName != \"\"'>AND v.holder_name LIKE CONCAT('%', #{holderName}, '%')</if>" +
        "  <if test='keyword != null and keyword != \"\"'>AND (v.voucher_code LIKE CONCAT('%', #{keyword}, '%') OR v.remark LIKE CONCAT('%', #{keyword}, '%'))</if>" +
        "  <if test='voucherType != null and voucherType != \"\"'>AND b.voucher_type = #{voucherType}</if>" +
        "  <if test='expireStart != null'>AND v.expire_at &gt;= #{expireStart}</if>" +
        "  <if test='expireEnd != null'>AND v.expire_at &lt;= #{expireEnd}</if>" +
        "  <if test='categoryId != null'>AND v.category_id = #{categoryId}</if>" +
        "</where>" +
        "ORDER BY v.created_at DESC" +
        "</script>")
    IPage<VoucherListRow> selectPagedWithBatch(Page<VoucherListRow> page,
                                                @Param("holderId") String holderId,
                                                @Param("holderName") String holderName,
                                                @Param("keyword") String keyword,
                                                @Param("voucherType") String voucherType,
                                                @Param("expireStart") LocalDateTime expireStart,
                                                @Param("expireEnd") LocalDateTime expireEnd,
                                                @Param("categoryId") Long categoryId);

    @Update("<script>" +
        "UPDATE voucher SET category_id = #{categoryId}, updated_at = NOW() " +
        "WHERE id IN " +
        "<foreach collection='voucherIds' item='id' open='(' separator=',' close=')'>" +
        "  #{id}" +
        "</foreach>" +
        "</script>")
    int batchUpdateCategory(@Param("voucherIds") List<Long> voucherIds,
                            @Param("categoryId") Long categoryId);

    @Update("UPDATE voucher SET category_id = NULL, updated_at = NOW() WHERE category_id = #{categoryId}")
    int clearCategoryByCategoryId(@Param("categoryId") Long categoryId);
}
