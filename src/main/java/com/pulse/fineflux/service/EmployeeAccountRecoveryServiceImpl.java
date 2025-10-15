package com.pulse.fineflux.service;

import com.pulse.fineflux.entity.Employee;
import com.pulse.fineflux.entity.EmployeePasswordResetToken;
import com.pulse.fineflux.repository.EmployeeRepository;
import com.pulse.fineflux.repository.EmployeePasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeAccountRecoveryServiceImpl implements EmployeeAccountRecoveryService {

    private final EmployeeRepository employeeRepo;
    private final EmployeePasswordResetTokenRepository tokenRepo;
    private final MailSenderService mailSenderService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void requestPasswordReset(String orgId, String emailOrUsername) {
        log.info("Received password reset request for orgId={} identifier={}", orgId, emailOrUsername);

        Optional<Employee> opt = employeeRepo.findByOrganizationIdAndUsername(orgId, emailOrUsername);
        if (opt.isEmpty()) {
            log.info("No employee found by username, trying email...");
            opt = employeeRepo.findByOrganizationIdAndEmailId(orgId, emailOrUsername);
        }
        if (opt.isPresent()) {
            Employee emp = opt.get();
            log.info("Employee found for password reset: id={}, username={}, email={}", emp.getId(), emp.getUsername(), emp.getEmailId());
            String token = UUID.randomUUID().toString();
            EmployeePasswordResetToken resetToken = EmployeePasswordResetToken.builder()
                    .token(token)
                    .orgId(orgId)
                    .employeeId(emp.getId())
                    .expiryDate(Instant.now().plusSeconds(3600)) // valid for 1 hour
                    .build();
            tokenRepo.save(resetToken);

            String link = "http://fineflux.com/reset-password?token=" + token + "&orgId=" + orgId;
            try {
                mailSenderService.sendPasswordResetMail(emp.getEmailId(), emp.getUsername(), link);
                log.info("Password reset mail sent to {}", emp.getEmailId());
            } catch (Exception e) {
                log.error("Failed to send password reset mail to {}: {}", emp.getEmailId(), e.getMessage(), e);
            }
        } else {
            log.warn("No employee found for password reset with orgId={} identifier={}", orgId, emailOrUsername);
        }
        // Always respond 200 OK to caller
    }

    @Override
    @Transactional
    public void resetPassword(String orgId, String token, String newPassword) {
        log.info("Received password reset (token) for orgId={} token={}", orgId, token);

        EmployeePasswordResetToken resetToken = tokenRepo.findByTokenAndOrgId(token, orgId)
                .orElseThrow(() -> {
                    log.warn("No password reset token found for token={} orgId={}", token, orgId);
                    return new RuntimeException("Invalid or expired token");
                });
        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            log.warn("Token expired for token={} orgId={}", token, orgId);
            tokenRepo.delete(resetToken);
            throw new RuntimeException("Token expired");
        }

        Employee emp = employeeRepo.findById(resetToken.getEmployeeId())
                .orElseThrow(() -> {
                    log.error("Password reset token employee not found for empId={}", resetToken.getEmployeeId());
                    return new RuntimeException("Employee not found for token");
                });
        emp.setPasswordHash(passwordEncoder.encode(newPassword));
        employeeRepo.save(emp);
        log.info("Password updated for employeeId={} username={}", emp.getId(), emp.getUsername());
        tokenRepo.delete(resetToken);
        log.info("Password reset token deleted for token={}", token);
        // Optionally: send confirmation mail
    }

    @Override
    public void sendUsernameByEmail(String orgId, String email) {
        log.info("Received forgot username request for orgId={} email={}", orgId, email);
        Optional<Employee> opt = employeeRepo.findByOrganizationIdAndEmailId(orgId, email);
        if (opt.isPresent()) {
            Employee emp = opt.get();
            log.info("User found for username recovery: id={}, username={}, email={}", emp.getId(), emp.getUsername(), email);
            try {
                mailSenderService.sendUsernameReminder(email, emp.getUsername(), emp.getFirstName());
                log.info("Sent username reminder email to {}", email);
            } catch (Exception ex) {
                log.error("Failed to send username reminder to {}: {}", email, ex.getMessage(), ex);
            }
        } else {
            log.warn("No user found for username recovery with orgId={} email={}", orgId, email);
        }
        // Always respond OK
    }
}
