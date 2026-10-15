package com.jcaa.accounts.domain.exception;

public final class InvalidUserEmailException extends DomainException {

  private static final String MESSAGE_INVALID = "The user email is not valid.";

  private InvalidUserEmailException(final String message) {
    super(message);
  }

  public static InvalidUserEmailException becauseFormatIsInvalid() {
    return new InvalidUserEmailException(MESSAGE_INVALID);
  }
}
