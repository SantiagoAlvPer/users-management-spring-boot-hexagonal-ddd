package com.jcaa.accounts.domain.exception;

public final class InvalidAccountBalanceException extends DomainException {

  private static final String MESSAGE_NULL = "The account balance is required.";
  private static final String MESSAGE_NEGATIVE = "The account balance must not be negative.";

  private InvalidAccountBalanceException(final String message) {
    super(message);
  }

  public static InvalidAccountBalanceException becauseValueIsNull() {
    return new InvalidAccountBalanceException(MESSAGE_NULL);
  }

  public static InvalidAccountBalanceException becauseValueIsNegative() {
    return new InvalidAccountBalanceException(MESSAGE_NEGATIVE);
  }
}
