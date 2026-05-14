package com.example.voucher.scheduled;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.voucher.entity.Voucher;
import com.example.voucher.mapper.VoucherMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoucherExpireTask {

    private final VoucherMapper voucherMapper;
    private static final int BATCH_SIZE = 500;

    @Scheduled(cron = "0 5 0 * * ?")
    public void expireVouchers() {
        log.info("Starting voucher expiration task...");
        int total = 0;
        while (true) {
            int updated = voucherMapper.update(
                new LambdaUpdateWrapper<Voucher>()
                    .set(Voucher::getStatus, "EXPIRED")
                    .lt(Voucher::getExpireAt, LocalDateTime.now())
                    .eq(Voucher::getStatus, "ISSUED")
                    .last("LIMIT " + BATCH_SIZE)
            );
            if (updated == 0) break;
            total += updated;
            log.debug("Expired {} vouchers in this batch", updated);
        }
        log.info("Voucher expiration task completed. Total expired: {}", total);
    }
}
