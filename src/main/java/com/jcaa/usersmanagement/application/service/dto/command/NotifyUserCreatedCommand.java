package com.jcaa.usersmanagement.application.service.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NotifyUserCreatedCommand(
    @NotBlank String userId,
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotBlank String role) {}
