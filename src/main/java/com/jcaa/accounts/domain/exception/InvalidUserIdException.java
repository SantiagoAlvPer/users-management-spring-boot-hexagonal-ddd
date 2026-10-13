package com.jcaa.accounts.domain.exception;

public final class InvalidUserIdException extends DomainException {

  private static final String MESSAGE_EMPTY = "The user id must not be empty.";
  private static final String MESSAGE_TOO_LONG = "The user id must have at most %d characters.";

  private InvalidUserIdException(final String message) {
    super(message);
  }

  public static InvalidUserIdException becauseValueIsEmpty() {
    return new InvalidUserIdException(MESSAGE_EMPTY);
  }

  public static InvalidUserIdException becauseValueIsTooLong(final int maxLength) {
    return new InvalidUserIdException(String.format(MESSAGE_TOO_LONG, maxLength));
  }
}
