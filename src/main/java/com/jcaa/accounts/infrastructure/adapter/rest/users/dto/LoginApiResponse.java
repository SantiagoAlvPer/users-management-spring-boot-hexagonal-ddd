package com.jcaa.accounts.infrastructure.adapter.rest.users.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Respuesta de {@code POST /api/auth/login} del servicio de Users. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginApiResponse(String accessToken, String tokenType, long expiresIn) {}
