package com.example.voucher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.voucher.entity.VoucherConsumption;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VoucherConsumptionMapper extends BaseMapper<VoucherConsumption> {

    default List<VoucherConsumption> selectByVoucherId(Long voucherId) {
        return selectList(
            Wrappers.<VoucherConsumption>lambdaQuery()
                .eq(VoucherConsumption::getVoucherId, voucherId)
                .orderByDesc(VoucherConsumption::getCreatedAt)
        );
    }
}
