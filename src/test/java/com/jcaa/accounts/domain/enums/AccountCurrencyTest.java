package com.jcaa.accounts.domain.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcaa.accounts.domain.exception.InvalidAccountCurrencyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AccountCurrency")
class AccountCurrencyTest {

  @Test
  @DisplayName("fromString() ignora mayusculas y espacios")
  void shouldParseIgnoringCase() {
    // Act & Assert
    assertEquals(AccountCurrency.USD, AccountCurrency.fromString(" usd "));
  }

  @Test
  @DisplayName("fromString() rechaza monedas no soportadas")
  void shouldRejectUnsupported() {
    // Act & Assert
    assertThrows(InvalidAccountCurrencyException.class, () -> AccountCurrency.fromString("EUR"));
  }
}
