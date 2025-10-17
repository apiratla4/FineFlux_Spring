package com.pulse.fineflux.domain;

import jakarta.validation.constraints.NotBlank;

public class ChangePasswordRequest {

    @NotBlank
    public String empId;

    @NotBlank
    public String currentPassword;

    @NotBlank
    public String newPassword;

    public ChangePasswordRequest() {}
}

