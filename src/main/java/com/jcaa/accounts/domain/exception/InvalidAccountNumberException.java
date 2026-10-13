package com.jcaa.accounts.domain.exception;

public final class InvalidAccountNumberException extends DomainException {

  private static final String MESSAGE_FORMAT = "The account number must have exactly %d digits.";

  private InvalidAccountNumberException(final String message) {
    super(message);
  }

  public static InvalidAccountNumberException becauseFormatIsInvalid(final int length) {
    return new InvalidAccountNumberException(String.format(MESSAGE_FORMAT, length));
  }
}
