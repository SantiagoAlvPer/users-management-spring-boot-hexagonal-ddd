package com.jcaa.usersmanagement.domain.exception;

public class FileStorageException extends RuntimeException {

  public FileStorageException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public FileStorageException(final String message) {
    super(message);
  }
}
