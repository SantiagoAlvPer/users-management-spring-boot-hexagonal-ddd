package com.jcaa.accounts.infrastructure.entrypoint.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body que envia el servicio de Users (Paso 5). {@code currency} es opcional (default COP). */
public record CreateAccountRestRequest(
    @NotBlank(message = "userId must not be blank")
        @Size(max = 36, message = "userId must have at most 36 characters")
        String userId,
    String currency) {}
