package com.example.voucher.controller;

import com.example.voucher.common.Result;
import com.example.voucher.dto.ChangePasswordRequest;
import com.example.voucher.dto.LoginRequest;
import com.example.voucher.dto.UpdateMeRequest;
import com.example.voucher.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Object> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @GetMapping("/me")
    public Result<Object> me(Authentication auth) {
        return Result.success(authService.getCurrentUser(auth.getName()));
    }

    @PutMapping("/me")
    public Result<Object> updateMe(Authentication auth,
                                   @Valid @RequestBody UpdateMeRequest request) {
        return Result.success(authService.updateCurrentUser(
            Long.valueOf(auth.getName()), request.getMobile(), request.getDepartment()));
    }

    @PostMapping("/change-password")
    public Result<Object> changePassword(Authentication auth,
                                         @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(auth.getName(), request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }
}
