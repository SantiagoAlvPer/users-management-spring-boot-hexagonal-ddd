package com.jcaa.accounts.infrastructure.adapter.persistence.exception;

public final class PersistenceException extends RuntimeException {

  private static final String MESSAGE_SAVE = "Failed to save account with ID: '%s'.";
  private static final String MESSAGE_FIND = "Failed to find account with ID: '%s'.";
  private static final String MESSAGE_FIND_BY_USER = "Failed to find account of user: '%s'.";
  private static final String MESSAGE_DELETE = "Failed to delete account with ID: '%s'.";
  private static final String MESSAGE_FIND_USER_EMAIL = "Failed to read email of user: '%s'.";

  private PersistenceException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public static PersistenceException becauseSaveFailed(
      final String accountId, final Throwable cause) {
    return new PersistenceException(String.format(MESSAGE_SAVE, accountId), cause);
  }

  public static PersistenceException becauseFindByIdFailed(
      final String accountId, final Throwable cause) {
    return new PersistenceException(String.format(MESSAGE_FIND, accountId), cause);
  }

  public static PersistenceException becauseFindByUserIdFailed(
      final String userId, final Throwable cause) {
    return new PersistenceException(String.format(MESSAGE_FIND_BY_USER, userId), cause);
  }

  public static PersistenceException becauseFindUserEmailFailed(
      final String userId, final Throwable cause) {
    return new PersistenceException(String.format(MESSAGE_FIND_USER_EMAIL, userId), cause);
  }

  public static PersistenceException becauseDeleteFailed(
      final String accountId, final Throwable cause) {
    return new PersistenceException(String.format(MESSAGE_DELETE, accountId), cause);
  }
}
