package com.jcaa.accounts.application.service.dto.query;

import jakarta.validation.constraints.NotBlank;

public record GetAccountByIdQuery(@NotBlank String id) {}
