package com.example.voucher.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleBusiness_shouldReturnErrorWithCode() {
        BusinessException ex = new BusinessException(4004, "券不存在");
        Result<Void> result = handler.handleBusiness(ex);
        assertEquals(4004, result.getCode());
        assertEquals("券不存在", result.getMessage());
    }

    @Test
    void handleBusiness_withDefaultCode_shouldReturnCode1001() {
        BusinessException ex = new BusinessException("参数错误");
        Result<Void> result = handler.handleBusiness(ex);
        assertEquals(1001, result.getCode());
    }

    @Test
    void handleBadCredentials_shouldReturn401() {
        BadCredentialsException ex = new BadCredentialsException("bad");
        Result<Void> result = handler.handleBadCredentials(ex);
        assertEquals(401, result.getCode());
        assertEquals("用户名或密码错误", result.getMessage());
    }

    @Test
    void handleValidation_shouldReturnFieldErrorMessage() {
        BeanPropertyBindingResult bindingResult =
            new BeanPropertyBindingResult(new Object(), "dto");
        bindingResult.addError(new FieldError("dto", "name", "名称不能为空"));
        MethodArgumentNotValidException ex =
            new MethodArgumentNotValidException(null, bindingResult);

        Result<Void> result = handler.handleValidation(ex);
        assertEquals(1001, result.getCode());
        assertEquals("名称不能为空", result.getMessage());
    }

    @Test
    void handleException_shouldReturn500() {
        Exception ex = new Exception("unexpected");
        Result<Void> result = handler.handleException(ex);
        assertEquals(500, result.getCode());
        assertEquals("系统内部错误", result.getMessage());
    }
}
