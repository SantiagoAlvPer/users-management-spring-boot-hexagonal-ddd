package com.jcaa.usersmanagement.domain.exception;

public class UserFileNotFoundException extends DomainException {

  public UserFileNotFoundException(final String message) {
    super(message);
  }

  public static UserFileNotFoundException becauseKeyWasNotFound(final String fileKey) {
    return new UserFileNotFoundException(
        "No se encontró el archivo solicitado: " + fileKey);
  }
}
