package com.jcaa.accounts.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcaa.accounts.domain.exception.InvalidAccountBalanceException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AccountBalance")
class AccountBalanceTest {

  @Test
  @DisplayName("zero() devuelve 0.00")
  void shouldCreateZeroWithScaleTwo() {
    // Act
    final AccountBalance balance = AccountBalance.zero();

    // Assert
    assertEquals(new BigDecimal("0.00"), balance.value());
  }

  @Test
  @DisplayName("rechaza saldo nulo")
  void shouldRejectNull() {
    // Act & Assert
    assertThrows(InvalidAccountBalanceException.class, () -> new AccountBalance(null));
  }

  @Test
  @DisplayName("rechaza saldo negativo")
  void shouldRejectNegative() {
    // Arrange
    final BigDecimal negative = new BigDecimal("-0.01");

    // Act & Assert
    assertThrows(InvalidAccountBalanceException.class, () -> new AccountBalance(negative));
  }
}
