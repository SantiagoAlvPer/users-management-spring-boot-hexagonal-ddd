package com.jcaa.accounts.application.service.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** {@code currency} es opcional: si viene vacio se usa COP. */
public record CreateAccountCommand(@NotBlank @Size(max = 36) String userId, String currency) {}
