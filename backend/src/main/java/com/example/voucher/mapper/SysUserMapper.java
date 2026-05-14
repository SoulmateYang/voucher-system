package com.example.voucher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.voucher.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE role = 'EMPLOYEE' AND enabled = 1 " +
            "AND (#{keyword} IS NULL OR #{keyword} = '' " +
            "OR real_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR employee_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR mobile LIKE CONCAT('%', #{keyword}, '%') " +
            "OR department LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY created_at DESC")
    IPage<SysUser> findEmployeesByKeyword(Page<?> page, @Param("keyword") String keyword);
}
