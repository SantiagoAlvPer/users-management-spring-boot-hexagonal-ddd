package com.jcaa.accounts.infrastructure.adapter.persistence.dto;

import java.math.BigDecimal;

public record AccountPersistenceDto(
    String id,
    String userId,
    String accountNumber,
    BigDecimal balance,
    String currency,
    String status) {}
