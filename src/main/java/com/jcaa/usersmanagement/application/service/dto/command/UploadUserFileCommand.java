package com.jcaa.usersmanagement.application.service.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UploadUserFileCommand(
    @NotBlank String userId,
    @NotBlank String originalFilename,
    @NotBlank String contentType,
    @Positive long sizeBytes,
    @NotNull byte[] content) {}
