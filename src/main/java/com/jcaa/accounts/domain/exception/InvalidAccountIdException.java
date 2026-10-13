package com.jcaa.accounts.domain.exception;

public final class InvalidAccountIdException extends DomainException {

  private static final String MESSAGE_EMPTY = "The account id must not be empty.";

  private InvalidAccountIdException(final String message) {
    super(message);
  }

  public static InvalidAccountIdException becauseValueIsEmpty() {
    return new InvalidAccountIdException(MESSAGE_EMPTY);
  }
}
