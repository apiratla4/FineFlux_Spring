package com.pulse.fineflux.domain;

import jakarta.validation.constraints.NotBlank;

public class ChangePasswordRequest {

    @NotBlank(message = "Employee ID is required")
    public String empId;

    @NotBlank(message = "Current password is required")
    public String currentPassword;

    @NotBlank(message = "New password is required")
    public String newPassword;

    public ChangePasswordRequest() {}
}
