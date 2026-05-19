package com.example.voucher.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VoucherTypeTest {

    @Test
    void isValid_shouldReturnTrueForResourceUsage() {
        assertTrue(VoucherType.isValid("RESOURCE_USAGE"));
    }

    @Test
    void isValid_shouldReturnTrueForCoupon() {
        assertTrue(VoucherType.isValid("COUPON"));
    }

    @Test
    void isValid_shouldReturnTrueForStoredValue() {
        assertTrue(VoucherType.isValid("STORED_VALUE"));
    }

    @Test
    void isValid_shouldReturnFalseForNull() {
        assertFalse(VoucherType.isValid(null));
    }

    @Test
    void isValid_shouldReturnFalseForUnknownType() {
        assertFalse(VoucherType.isValid("INVALID"));
    }

    @Test
    void isCoupon_shouldReturnTrueForCouponType() {
        assertTrue(VoucherType.isCoupon("COUPON"));
    }

    @Test
    void isCoupon_shouldReturnFalseForResourceUsage() {
        assertFalse(VoucherType.isCoupon("RESOURCE_USAGE"));
    }

    @Test
    void isStoredValue_shouldReturnTrueForStoredValue() {
        assertTrue(VoucherType.isStoredValue("STORED_VALUE"));
    }

    @Test
    void isStoredValue_shouldReturnFalseForCoupon() {
        assertFalse(VoucherType.isStoredValue("COUPON"));
    }

    @Test
    void validateDiscountType_shouldAcceptFixedAmount() {
        assertDoesNotThrow(() -> VoucherType.validateDiscountType("FIXED_AMOUNT"));
    }

    @Test
    void validateDiscountType_shouldAcceptPercentage() {
        assertDoesNotThrow(() -> VoucherType.validateDiscountType("PERCENTAGE"));
    }

    @Test
    void validateDiscountType_shouldThrowForInvalidType() {
        BusinessException ex = assertThrows(BusinessException.class,
            () -> VoucherType.validateDiscountType("INVALID"));
        assertTrue(ex.getMessage().contains("折扣类型"));
    }

    @Test
    void constants_shouldHaveCorrectValues() {
        assertEquals("RESOURCE_USAGE", VoucherType.RESOURCE_USAGE);
        assertEquals("COUPON", VoucherType.COUPON);
        assertEquals("STORED_VALUE", VoucherType.STORED_VALUE);
        assertEquals(3, VoucherType.ALL_TYPES.size());
    }
}
