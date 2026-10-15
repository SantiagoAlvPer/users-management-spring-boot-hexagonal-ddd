package com.jcaa.accounts.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcaa.accounts.domain.exception.InvalidUserEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("UserEmail")
class UserEmailTest {

  @Test
  @DisplayName("normaliza espacios y mayusculas")
  void shouldNormalize() {
    // Act
    final UserEmail email = new UserEmail("  John@Example.COM ");

    // Assert
    assertEquals("john@example.com", email.value());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "john", "john@", "@example.com", "john@example", "jo hn@example.com"})
  @DisplayName("rechaza formatos invalidos")
  void shouldRejectInvalidFormat(final String value) {
    // Act & Assert
    assertThrows(InvalidUserEmailException.class, () -> new UserEmail(value));
  }
}
