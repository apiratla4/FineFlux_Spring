package com.pulse.fineflux.controller;

import com.pulse.fineflux.service.EmployeeAccountRecoveryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/organizations/{orgId}/employees")
@RequiredArgsConstructor
public class EmployeeAccountRecoveryController {

    private final EmployeeAccountRecoveryService service;

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> requestPasswordReset(@PathVariable("orgId") String orgId, @RequestBody ForgotPasswordRequest request) {
        log.info("API: Forgot password requested for orgId={} identifier={}", orgId, request.emailOrUsername);
        service.requestPasswordReset(orgId, request.emailOrUsername);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable("orgId") String orgId, @RequestBody ResetPasswordRequest request) {
        log.info("API: Reset password (token) for orgId={} token={}", orgId, request.token);
        service.resetPassword(orgId, request.token, request.newPassword);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-username")
    public ResponseEntity<Void> sendUsername(@PathVariable("orgId") String orgId, @RequestBody ForgotUsernameRequest req) {
        log.info("API: Forgot username requested for orgId={} email={}", orgId, req.email);
        service.sendUsernameByEmail(orgId, req.email);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class ForgotPasswordRequest { private String emailOrUsername; }
    @Data
    public static class ResetPasswordRequest { private String token; private String newPassword; }
    @Data
    public static class ForgotUsernameRequest { private String email; }
}
