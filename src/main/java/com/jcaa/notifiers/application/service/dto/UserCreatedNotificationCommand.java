package com.jcaa.notifiers.application.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreatedNotificationCommand(
    @NotBlank(message = "El nombre es obligatorio")
    String name,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo no válido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    String password,

    String role
) {}
