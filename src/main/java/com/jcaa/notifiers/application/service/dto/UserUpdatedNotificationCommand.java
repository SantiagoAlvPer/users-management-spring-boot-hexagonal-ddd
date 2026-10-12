package com.jcaa.notifiers.application.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserUpdatedNotificationCommand(
    @NotBlank(message = "El nombre es obligatorio")
    String name,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo no válido")
    String email,

    String role,
    String status
) {}
