package com.example.voucher.controller;

import com.example.voucher.common.Result;
import com.example.voucher.service.CallbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/callback")
@RequiredArgsConstructor
public class CallbackController {

    private final CallbackService callbackService;

    @PostMapping("/approval")
    public Result<Object> approval(@RequestBody String payload,
                                    @RequestHeader("X-Signature") String signature) {
        return Result.success(callbackService.handleApproval(payload, signature));
    }
}
