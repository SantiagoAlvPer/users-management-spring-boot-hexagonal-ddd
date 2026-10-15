package com.jcaa.accounts.infrastructure.adapter.rest.users.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Respuesta de {@code GET /api/users/{id}} del servicio de Users. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserApiResponse(String id, String name, String email, String role, String status) {}
