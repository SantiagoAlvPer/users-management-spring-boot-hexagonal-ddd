package com.jcaa.accounts.domain.valueobject;

import com.jcaa.accounts.domain.exception.InvalidAccountIdException;
import java.util.Objects;
import java.util.UUID;

public record AccountId(String value) {

  public AccountId {
    final String normalizedValue =
        Objects.requireNonNull(value, "AccountId cannot be null").trim();
    if (normalizedValue.isEmpty()) {
      throw InvalidAccountIdException.becauseValueIsEmpty();
    }
    value = normalizedValue;
  }

  public static AccountId generate() {
    return new AccountId(UUID.randomUUID().toString());
  }

  @Override
  public String toString() {
    return value;
  }
}
