package com.example.voucher.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.voucher.common.BusinessException;
import com.example.voucher.dto.CreateEmployeeRequest;
import com.example.voucher.dto.UpdateEmployeeRequest;
import com.example.voucher.entity.SysUser;
import com.example.voucher.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public IPage<SysUser> list(int page, int pageSize, String keyword) {
        Page<SysUser> p = new Page<>(page, pageSize);
        return userMapper.findEmployeesByKeyword(p,
            (keyword == null || keyword.isBlank()) ? null : keyword.trim());
    }

    public SysUser getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null || !"EMPLOYEE".equals(user.getRole())) {
            throw new BusinessException("员工不存在");
        }
        return user;
    }

    public Map<String, Object> create(CreateEmployeeRequest request) {
        SysUser exists = userMapper.selectOne(
            Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, request.getUsername()));
        if (exists != null) {
            throw new BusinessException("工号已存在");
        }

        String rawPassword = generatePassword();
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setEmployeeNo(request.getUsername());
        user.setMobile(request.getMobile());
        user.setDepartment(request.getDepartment());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole("EMPLOYEE");
        user.setEnabled(1);
        userMapper.insert(user);

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("employeeNo", user.getEmployeeNo());
        result.put("mobile", user.getMobile());
        result.put("department", user.getDepartment());
        result.put("initialPassword", rawPassword);
        return result;
    }

    public void update(Long id, UpdateEmployeeRequest request) {
        SysUser user = getById(id);
        user.setRealName(request.getRealName());
        user.setMobile(request.getMobile());
        user.setDepartment(request.getDepartment());
        userMapper.updateById(user);
    }

    public void delete(Long id, String currentUserId) {
        if (String.valueOf(id).equals(currentUserId)) {
            throw new BusinessException("不可删除当前登录用户");
        }
        SysUser user = getById(id);
        user.setEnabled(0);
        userMapper.updateById(user);
    }

    public String resetPassword(Long id) {
        SysUser user = getById(id);
        String rawPassword = generatePassword();
        user.setPassword(passwordEncoder.encode(rawPassword));
        userMapper.updateById(user);
        return rawPassword;
    }

    private String generatePassword() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
