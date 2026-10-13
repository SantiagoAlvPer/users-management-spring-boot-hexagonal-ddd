package com.jcaa.accounts.domain.exception;

public final class AccountNotFoundException extends DomainException {

  private static final String MESSAGE_BY_ID = "The account with id '%s' was not found.";
  private static final String MESSAGE_BY_USER_ID = "The user with id '%s' has no account.";

  private AccountNotFoundException(final String message) {
    super(message);
  }

  public static AccountNotFoundException becauseIdWasNotFound(final String accountId) {
    return new AccountNotFoundException(String.format(MESSAGE_BY_ID, accountId));
  }

  public static AccountNotFoundException becauseUserHasNoAccount(final String userId) {
    return new AccountNotFoundException(String.format(MESSAGE_BY_USER_ID, userId));
  }
}
