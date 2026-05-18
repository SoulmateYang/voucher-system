package com.example.voucher.service;

import com.example.voucher.common.BusinessException;
import com.example.voucher.dto.CreateEmployeeRequest;
import com.example.voucher.dto.UpdateEmployeeRequest;
import com.example.voucher.entity.SysUser;
import com.example.voucher.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EmployeeServiceTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Clean test data
        userMapper.delete(null);
        SysUser emp = new SysUser();
        emp.setId(100L);
        emp.setUsername("E001");
        emp.setRealName("张三");
        emp.setRole("EMPLOYEE");
        emp.setEnabled(1);
        emp.setPassword(passwordEncoder.encode("test123"));
        userMapper.insert(emp);
    }

    @Test
    void list_shouldReturnPagedEmployees() {
        var page = employeeService.list(1, 10, null);
        assertTrue(page.getTotal() > 0);
    }

    @Test
    void list_shouldFilterByKeyword() {
        var page = employeeService.list(1, 10, "张三");
        assertTrue(page.getTotal() > 0);
        var page2 = employeeService.list(1, 10, "NONEXISTENT");
        assertEquals(0, page2.getTotal());
    }

    @Test
    void getById_shouldReturnEmployee() {
        SysUser user = employeeService.getById(100L);
        assertEquals("E001", user.getUsername());
        assertEquals("张三", user.getRealName());
    }

    @Test
    void getById_shouldThrowForNonEmployee() {
        SysUser admin = new SysUser();
        admin.setId(200L);
        admin.setUsername("admin_test");
        admin.setRole("ADMIN");
        admin.setEnabled(1);
        admin.setPassword(passwordEncoder.encode("admin123"));
        userMapper.insert(admin);

        assertThrows(BusinessException.class, () -> employeeService.getById(200L));
    }

    @Test
    void getById_shouldThrowForNonExistentId() {
        assertThrows(BusinessException.class, () -> employeeService.getById(99999L));
    }

    @Test
    void create_shouldCreateEmployeeWithGeneratedPassword() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setUsername("E002");
        request.setRealName("李四");

        var result = employeeService.create(request);
        assertNotNull(result.get("id"));
        assertEquals("E002", result.get("username"));
        assertNotNull(result.get("initialPassword"));
    }

    @Test
    void create_shouldThrowForDuplicateUsername() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setUsername("E001"); // already exists
        request.setRealName("王五");

        assertThrows(BusinessException.class, () -> employeeService.create(request));
    }

    @Test
    void update_shouldUpdateEmployeeInfo() {
        UpdateEmployeeRequest request = new UpdateEmployeeRequest();
        request.setRealName("张三三");
        request.setMobile("13800138000");
        request.setDepartment("技术部");

        employeeService.update(100L, request);
        SysUser updated = userMapper.selectById(100L);
        assertEquals("张三三", updated.getRealName());
    }

    @Test
    void delete_shouldDisableEmployee() {
        employeeService.delete(100L, "admin_user");
        SysUser disabled = userMapper.selectById(100L);
        assertEquals(0, disabled.getEnabled());
    }

    @Test
    void delete_shouldThrowForSelfDelete() {
        assertThrows(BusinessException.class, () -> employeeService.delete(100L, "100"));
    }

    @Test
    void resetPassword_shouldReturnNewPassword() {
        String newPwd = employeeService.resetPassword(100L);
        assertNotNull(newPwd);
        assertEquals(8, newPwd.length());
    }
}
