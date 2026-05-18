package com.example.voucher.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void constructor_withCodeAndMessage_shouldStoreBoth() {
        BusinessException ex = new BusinessException(4001, "券不存在");
        assertEquals(4001, ex.getCode());
        assertEquals("券不存在", ex.getMessage());
    }

    @Test
    void constructor_withMessageOnly_shouldUseDefaultCode() {
        BusinessException ex = new BusinessException("参数错误");
        assertEquals(1001, ex.getCode());
        assertEquals("参数错误", ex.getMessage());
    }

    @Test
    void shouldBeRuntimeException() {
        BusinessException ex = new BusinessException("test");
        assertInstanceOf(RuntimeException.class, ex);
    }
}
