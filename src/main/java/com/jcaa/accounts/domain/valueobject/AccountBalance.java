package com.jcaa.accounts.domain.valueobject;

import com.jcaa.accounts.domain.exception.InvalidAccountBalanceException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record AccountBalance(BigDecimal value) {

  private static final int SCALE = 2;

  public AccountBalance {
    if (Objects.isNull(value)) {
      throw InvalidAccountBalanceException.becauseValueIsNull();
    }
    if (value.signum() < 0) {
      throw InvalidAccountBalanceException.becauseValueIsNegative();
    }
    value = value.setScale(SCALE, RoundingMode.HALF_EVEN);
  }

  public static AccountBalance zero() {
    return new AccountBalance(BigDecimal.ZERO);
  }

  @Override
  public String toString() {
    return value.toPlainString();
  }
}
