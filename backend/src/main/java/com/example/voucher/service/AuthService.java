package com.example.voucher.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.voucher.common.BusinessException;
import com.example.voucher.common.JwtUtil;
import com.example.voucher.dto.LoginRequest;
import com.example.voucher.entity.SysUser;
import com.example.voucher.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public Map<String, Object> login(LoginRequest request) {
        SysUser user = userMapper.selectOne(
            Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, request.getUsername())
        );
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getEnabled() == 0) {
            throw new BusinessException(401, "账号已被禁用");
        }

        String token = jwtUtil.generateToken(String.valueOf(user.getId()), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("role", user.getRole());
        result.put("user", userInfo);
        return result;
    }

    public SysUser getCurrentUser(String userId) {
        return userMapper.selectById(Long.valueOf(userId));
    }

    public String getUserName(String userId) {
        SysUser user = userMapper.selectById(Long.valueOf(userId));
        return user != null && user.getRealName() != null ? user.getRealName() : userId;
    }

    public void changePassword(String userId, String oldPwd, String newPwd) {
        SysUser user = userMapper.selectById(Long.valueOf(userId));
        if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPwd));
        userMapper.updateById(user);
    }

    public SysUser updateCurrentUser(Long userId, String mobile, String department) {
        SysUser user = userMapper.selectById(userId);
        user.setMobile(mobile);
        user.setDepartment(department);
        userMapper.updateById(user);
        return user;
    }
}
