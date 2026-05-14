package com.example.voucher.common;

import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Generates 32-char voucher codes: prefix(2) + snowflake-base32(26) + checksum(4)
 */
@Component
public class VoucherCodeUtil {

    private static final String BASE32 = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int BASE32_MASK = 0x1F;

    @Value("${app.voucher-code.prefix:SN}")
    private String prefix;

    public String generate() {
        long snowflakeId = IdUtil.getSnowflakeNextId();
        String base32Part = toBase32(snowflakeId);
        String raw = prefix + base32Part;
        String checksum = String.format("%04d", Math.abs(raw.hashCode()) % 10000);
        return raw + checksum;
    }

    private String toBase32(long value) {
        StringBuilder sb = new StringBuilder();
        long v = value;
        while (v > 0) {
            sb.insert(0, BASE32.charAt((int) (v & BASE32_MASK)));
            v >>>= 5;
        }
        while (sb.length() < 26) {
            sb.insert(0, BASE32.charAt(0));
        }
        if (sb.length() > 26) {
            sb.setLength(26);
        }
        return sb.toString();
    }
}
