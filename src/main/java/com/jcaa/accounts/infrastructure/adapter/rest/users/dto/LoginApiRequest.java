package com.jcaa.accounts.infrastructure.adapter.rest.users.dto;

/** Body de {@code POST /api/auth/login} del servicio de Users. */
public record LoginApiRequest(String email, String password) {}
