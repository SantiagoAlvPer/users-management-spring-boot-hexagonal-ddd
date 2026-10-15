package com.jcaa.accounts.domain.valueobject;

import com.jcaa.accounts.domain.exception.InvalidUserEmailException;
import java.util.Objects;
import java.util.regex.Pattern;

/** Correo del dueno de la cuenta, obtenido del servicio de Users. No se persiste en accounts. */
public record UserEmail(String value) {

  private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

  public UserEmail {
    final String normalizedValue =
        Objects.requireNonNull(value, "UserEmail cannot be null").trim().toLowerCase();
    if (!FORMAT.matcher(normalizedValue).matches()) {
      throw InvalidUserEmailException.becauseFormatIsInvalid();
    }
    value = normalizedValue;
  }

  @Override
  public String toString() {
    return value;
  }
}
