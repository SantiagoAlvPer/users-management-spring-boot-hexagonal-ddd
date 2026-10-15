package com.jcaa.accounts.domain.exception;

public final class InvalidUserNameException extends DomainException {

  private static final String MESSAGE_EMPTY = "The user name must not be empty.";

  private InvalidUserNameException(final String message) {
    super(message);
  }

  public static InvalidUserNameException becauseValueIsEmpty() {
    return new InvalidUserNameException(MESSAGE_EMPTY);
  }
}
