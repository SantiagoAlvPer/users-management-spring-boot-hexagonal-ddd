package com.jcaa.accounts.domain.enums;

import com.jcaa.accounts.domain.exception.InvalidAccountCurrencyException;
import java.util.Arrays;
import java.util.Objects;

public enum AccountCurrency {
  COP,
  USD;

  public static AccountCurrency defaultCurrency() {
    return COP;
  }

  public static AccountCurrency fromString(final String value) {
    if (Objects.isNull(value)) {
      throw InvalidAccountCurrencyException.becauseValueIsInvalid(null);
    }
    final String normalized = value.trim().toUpperCase();
    return Arrays.stream(values())
        .filter(currency -> currency.name().equals(normalized))
        .findFirst()
        .orElseThrow(() -> InvalidAccountCurrencyException.becauseValueIsInvalid(value));
  }
}
