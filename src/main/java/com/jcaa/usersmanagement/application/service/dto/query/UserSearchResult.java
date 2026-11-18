package com.jcaa.usersmanagement.application.service.dto.query;

public record UserSearchResult(
    String id, String name, String email, String role, String status) {}
