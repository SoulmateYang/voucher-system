package com.example.voucher.common;

import java.util.Set;

public final class VoucherType {

    public static final String RESOURCE_USAGE = "RESOURCE_USAGE";
    public static final String COUPON = "COUPON";
    public static final String STORED_VALUE = "STORED_VALUE";

    public static final Set<String> DISCOUNT_TYPES = Set.of("FIXED_AMOUNT", "PERCENTAGE");
    public static final Set<String> ALL_TYPES = Set.of(RESOURCE_USAGE, COUPON, STORED_VALUE);

    private VoucherType() {
    }

    public static boolean isValid(String type) {
        return type != null && ALL_TYPES.contains(type);
    }

    public static boolean isCoupon(String type) {
        return COUPON.equals(type);
    }

    public static boolean isStoredValue(String type) {
        return STORED_VALUE.equals(type);
    }

    public static void validateDiscountType(String discountType) {
        if (!DISCOUNT_TYPES.contains(discountType)) {
            throw new BusinessException("折扣类型无效，必须为 FIXED_AMOUNT 或 PERCENTAGE");
        }
    }
}
