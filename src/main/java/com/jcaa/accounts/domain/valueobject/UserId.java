package com.jcaa.accounts.domain.valueobject;

import com.jcaa.accounts.domain.exception.InvalidUserIdException;
import java.util.Objects;

/** Identificador del usuario dueno de la cuenta (viene del servicio de Users). */
public record UserId(String value) {

  private static final int MAX_LENGTH = 36;

  public UserId {
    final String normalizedValue = Objects.requireNonNull(value, "UserId cannot be null").trim();
    if (normalizedValue.isEmpty()) {
      throw InvalidUserIdException.becauseValueIsEmpty();
    }
    if (normalizedValue.length() > MAX_LENGTH) {
      throw InvalidUserIdException.becauseValueIsTooLong(MAX_LENGTH);
    }
    value = normalizedValue;
  }

  @Override
  public String toString() {
    return value;
  }
}
