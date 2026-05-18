package com.example.voucher.service;

import com.example.voucher.common.BusinessException;
import com.example.voucher.dto.LoginRequest;
import com.example.voucher.entity.SysUser;
import com.example.voucher.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        if (userMapper.selectById(1L) == null) {
            SysUser admin = new SysUser();
            admin.setId(1L);
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealName("管理员");
            admin.setRole("ADMIN");
            admin.setEnabled(1);
            userMapper.insert(admin);
        }
    }

    @Test
    void login_shouldReturnTokenForValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        var result = authService.login(request);
        assertNotNull(result.get("token"));
        assertEquals("admin", ((java.util.Map<?, ?>) result.get("user")).get("username"));
    }

    @Test
    void login_shouldThrowForWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrongpassword");

        BusinessException ex = assertThrows(BusinessException.class,
            () -> authService.login(request));
        assertEquals(401, ex.getCode());
    }

    @Test
    void login_shouldThrowForNonExistentUser() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("admin123");

        BusinessException ex = assertThrows(BusinessException.class,
            () -> authService.login(request));
        assertEquals(401, ex.getCode());
    }

    @Test
    void login_shouldThrowForDisabledUser() {
        SysUser disabled = new SysUser();
        disabled.setId(2L);
        disabled.setUsername("disabled");
        disabled.setPassword(passwordEncoder.encode("admin123"));
        disabled.setRealName("已禁用");
        disabled.setRole("ADMIN");
        disabled.setEnabled(0);
        if (userMapper.selectById(2L) == null) {
            userMapper.insert(disabled);
        }

        LoginRequest request = new LoginRequest();
        request.setUsername("disabled");
        request.setPassword("admin123");

        BusinessException ex = assertThrows(BusinessException.class,
            () -> authService.login(request));
        assertEquals("账号已被禁用", ex.getMessage());
    }

    @Test
    void getCurrentUser_shouldReturnUserForValidId() {
        var user = authService.getCurrentUser("1");
        assertNotNull(user);
        assertEquals("admin", user.getUsername());
    }

    @Test
    void getUserName_shouldReturnRealName() {
        String name = authService.getUserName("1");
        assertEquals("管理员", name);
    }

    @Test
    void getUserName_shouldReturnIdForNonExistentUser() {
        String name = authService.getUserName("999");
        assertEquals("999", name);
    }
}
