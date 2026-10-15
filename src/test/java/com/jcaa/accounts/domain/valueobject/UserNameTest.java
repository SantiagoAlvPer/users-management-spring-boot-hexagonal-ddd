package com.jcaa.accounts.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcaa.accounts.domain.exception.InvalidUserNameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserName")
class UserNameTest {

  @Test
  @DisplayName("normaliza espacios")
  void shouldTrim() {
    // Act
    final UserName name = new UserName("  John Arrieta ");

    // Assert
    assertEquals("John Arrieta", name.value());
  }

  @Test
  @DisplayName("rechaza nombre vacio o nulo")
  void shouldRejectEmpty() {
    // Act & Assert
    assertThrows(InvalidUserNameException.class, () -> new UserName("   "));
    assertThrows(InvalidUserNameException.class, () -> new UserName(null));
  }
}
