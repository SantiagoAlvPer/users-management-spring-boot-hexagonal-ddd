package com.jcaa.accounts.domain.exception;

public final class InvalidAccountCurrencyException extends DomainException {

  private static final String MESSAGE_INVALID = "The currency '%s' is not supported.";

  private InvalidAccountCurrencyException(final String message) {
    super(message);
  }

  public static InvalidAccountCurrencyException becauseValueIsInvalid(final String value) {
    return new InvalidAccountCurrencyException(String.format(MESSAGE_INVALID, value));
  }
}
