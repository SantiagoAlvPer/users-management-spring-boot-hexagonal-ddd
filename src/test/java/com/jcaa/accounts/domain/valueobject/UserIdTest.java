package com.jcaa.accounts.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcaa.accounts.domain.exception.InvalidUserIdException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserId")
class UserIdTest {

  @Test
  @DisplayName("normaliza espacios")
  void shouldTrimValue() {
    // Act
    final UserId userId = new UserId("  u-1  ");

    // Assert
    assertEquals("u-1", userId.value());
  }

  @Test
  @DisplayName("rechaza valor vacio")
  void shouldRejectEmpty() {
    // Act & Assert
    assertThrows(InvalidUserIdException.class, () -> new UserId("   "));
  }

  @Test
  @DisplayName("rechaza valor de mas de 36 caracteres")
  void shouldRejectTooLong() {
    // Arrange
    final String tooLong = "x".repeat(37);

    // Act & Assert
    assertThrows(InvalidUserIdException.class, () -> new UserId(tooLong));
  }
}
