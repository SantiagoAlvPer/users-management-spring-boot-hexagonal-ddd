package com.jcaa.accounts.domain.valueobject;

import com.jcaa.accounts.domain.exception.InvalidUserNameException;
import java.util.Objects;

/** Nombre del dueno de la cuenta, leido de la tabla de Users. No se persiste en accounts. */
public record UserName(String value) {

  public UserName {
    final String normalizedValue = Objects.isNull(value) ? "" : value.trim();
    if (normalizedValue.isEmpty()) {
      throw InvalidUserNameException.becauseValueIsEmpty();
    }
    value = normalizedValue;
  }

  @Override
  public String toString() {
    return value;
  }
}
