package com.jcaa.accounts.domain.valueobject;

import com.jcaa.accounts.domain.exception.InvalidAccountNumberException;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.regex.Pattern;

/** Numero de cuenta de 10 digitos, visible para el usuario. */
public record AccountNumber(String value) {

  private static final int LENGTH = 10;
  private static final Pattern FORMAT = Pattern.compile("\\d{" + LENGTH + "}");
  private static final SecureRandom RANDOM = new SecureRandom();

  public AccountNumber {
    final String normalizedValue =
        Objects.requireNonNull(value, "AccountNumber cannot be null").trim();
    if (!FORMAT.matcher(normalizedValue).matches()) {
      throw InvalidAccountNumberException.becauseFormatIsInvalid(LENGTH);
    }
    value = normalizedValue;
  }

  public static AccountNumber generate() {
    final StringBuilder digits = new StringBuilder(LENGTH);
    // primer digito distinto de 0 para que el numero no pierda ceros a la izquierda
    digits.append(1 + RANDOM.nextInt(9));
    for (int i = 1; i < LENGTH; i++) {
      digits.append(RANDOM.nextInt(10));
    }
    return new AccountNumber(digits.toString());
  }

  @Override
  public String toString() {
    return value;
  }
}
