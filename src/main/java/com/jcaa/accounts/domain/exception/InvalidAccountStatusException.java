package com.jcaa.accounts.domain.exception;

public final class InvalidAccountStatusException extends DomainException {

  private static final String MESSAGE_INVALID = "The account status '%s' is not valid.";

  private InvalidAccountStatusException(final String message) {
    super(message);
  }

  public static InvalidAccountStatusException becauseValueIsInvalid(final String value) {
    return new InvalidAccountStatusException(String.format(MESSAGE_INVALID, value));
  }
}
