package com.jcaa.notifiers.application.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AccountCreatedNotificationCommand(
    String name,
    @NotBlank(message = "El correo de destino es obligatorio")
    @Email(message = "El formato de correo no es valido")
    String email,
    @NotBlank(message = "El numero de cuenta es obligatorio")
    String accountNumber,
    String balance,
    String currency,
    String status) {}
