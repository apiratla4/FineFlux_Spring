package com.pulse.fineflux.service;

public interface EmployeeAccountRecoveryService {
    void requestPasswordReset(String orgId, String emailOrUsername);
    void resetPassword(String orgId, String token, String newPassword);
    void sendUsernameByEmail(String orgId, String email);

}
