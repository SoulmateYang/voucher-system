package com.example.voucher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.voucher.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
