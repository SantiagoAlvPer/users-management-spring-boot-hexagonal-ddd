package com.jcaa.usersmanagement.application.service.dto.command;

import jakarta.validation.constraints.NotBlank;

public record DeleteUserFileCommand(@NotBlank String userId, @NotBlank String fileKey) {}
