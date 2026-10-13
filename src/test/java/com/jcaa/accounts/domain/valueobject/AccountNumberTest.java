package com.jcaa.accounts.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jcaa.accounts.domain.exception.InvalidAccountNumberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("AccountNumber")
class AccountNumberTest {

  @Test
  @DisplayName("generate() crea un numero de 10 digitos que no empieza por 0")
  void shouldGenerateTenDigits() {
    // Act
    final AccountNumber number = AccountNumber.generate();

    // Assert
    assertAll(
        () -> assertTrue(number.value().matches("\\d{10}")),
        () -> assertNotEquals('0', number.value().charAt(0)));
  }

  @Test
  @DisplayName("normaliza espacios alrededor del valor")
  void shouldTrimValue() {
    // Act
    final AccountNumber number = new AccountNumber("  1234567890 ");

    // Assert
    assertEquals("1234567890", number.value());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "123", "12345678901", "12345abcde"})
  @DisplayName("rechaza valores que no son 10 digitos")
  void shouldRejectInvalidFormat(final String value) {
    // Act & Assert
    assertThrows(InvalidAccountNumberException.class, () -> new AccountNumber(value));
  }
}
