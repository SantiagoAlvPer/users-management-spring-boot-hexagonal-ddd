package com.jcaa.accounts.application.service.dto.command;

import jakarta.validation.constraints.NotBlank;

public record DeleteAccountCommand(@NotBlank String id) {}
