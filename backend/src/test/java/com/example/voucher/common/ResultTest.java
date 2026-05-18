package com.example.voucher.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void success_withData_shouldReturnCodeZero() {
        Result<String> result = Result.success("hello");
        assertEquals(0, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("hello", result.getData());
    }

    @Test
    void success_withoutData_shouldReturnNullData() {
        Result<Void> result = Result.success();
        assertEquals(0, result.getCode());
        assertNull(result.getData());
    }

    @Test
    void error_withCodeAndMessage_shouldStoreBoth() {
        Result<Void> result = Result.error(401, "未登录");
        assertEquals(401, result.getCode());
        assertEquals("未登录", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void error_withMessageOnly_shouldUseDefaultCode1001() {
        Result<Void> result = Result.error("业务错误");
        assertEquals(1001, result.getCode());
        assertEquals("业务错误", result.getMessage());
    }
}
