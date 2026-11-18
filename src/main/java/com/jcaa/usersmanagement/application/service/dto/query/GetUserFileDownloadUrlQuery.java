package com.jcaa.usersmanagement.application.service.dto.query;

import jakarta.validation.constraints.NotBlank;

public record GetUserFileDownloadUrlQuery(@NotBlank String userId, @NotBlank String fileKey) {}
