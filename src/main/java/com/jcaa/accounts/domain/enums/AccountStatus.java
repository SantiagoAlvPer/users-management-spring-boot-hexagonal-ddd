package com.jcaa.accounts.domain.enums;

import com.jcaa.accounts.domain.exception.InvalidAccountStatusException;
import java.util.Arrays;
import java.util.Objects;

public enum AccountStatus {
  ACTIVE,
  BLOCKED,
  CLOSED;

  public static AccountStatus fromString(final String value) {
    if (Objects.isNull(value)) {
      throw InvalidAccountStatusException.becauseValueIsInvalid(null);
    }
    final String normalized = value.trim().toUpperCase();
    return Arrays.stream(values())
        .filter(status -> status.name().equals(normalized))
        .findFirst()
        .orElseThrow(() -> InvalidAccountStatusException.becauseValueIsInvalid(value));
  }
}
