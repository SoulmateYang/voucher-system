package com.example.voucher.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class VoucherCodeUtilTest {

    private VoucherCodeUtil voucherCodeUtil;

    @BeforeEach
    void setUp() {
        voucherCodeUtil = new VoucherCodeUtil();
        ReflectionTestUtils.setField(voucherCodeUtil, "prefix", "SN");
    }

    @Test
    void generate_shouldReturn32CharCode() {
        String code = voucherCodeUtil.generate();
        assertEquals(32, code.length());
    }

    @Test
    void generate_shouldStartWithPrefix() {
        String code = voucherCodeUtil.generate();
        assertTrue(code.startsWith("SN"));
    }

    @Test
    void generate_shouldEndWith4DigitChecksum() {
        String code = voucherCodeUtil.generate();
        String checksum = code.substring(28);
        assertEquals(4, checksum.length());
        assertTrue(checksum.matches("\\d{4}"));
    }

    @Test
    void generate_shouldProduceUniqueCodes() {
        String code1 = voucherCodeUtil.generate();
        String code2 = voucherCodeUtil.generate();
        assertNotEquals(code1, code2);
    }

    @Test
    void generate_customPrefix_shouldUseGivenPrefix() {
        ReflectionTestUtils.setField(voucherCodeUtil, "prefix", "XY");
        String code = voucherCodeUtil.generate();
        assertEquals(32, code.length());
        assertTrue(code.startsWith("XY"));
    }
}
