package com.jcaa.accounts.infrastructure.entrypoint.rest.dto.response;

import java.math.BigDecimal;

public record AccountRestResponse(
    String id,
    String userId,
    String accountNumber,
    BigDecimal balance,
    String currency,
    String status) {}
