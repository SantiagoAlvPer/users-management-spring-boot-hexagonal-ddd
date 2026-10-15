package com.jcaa.accounts.domain.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jcaa.accounts.domain.enums.AccountCurrency;
import com.jcaa.accounts.domain.enums.AccountStatus;
import com.jcaa.accounts.domain.valueobject.AccountBalance;
import com.jcaa.accounts.domain.valueobject.AccountId;
import com.jcaa.accounts.domain.valueobject.AccountNumber;
import com.jcaa.accounts.domain.valueobject.UserEmail;
import com.jcaa.accounts.domain.valueobject.UserName;
import com.jcaa.accounts.domain.valueobject.UserId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AccountModel")
class AccountModelTest {

  @Test
  @DisplayName("open() crea la cuenta ACTIVE con saldo cero")
  void shouldOpenActiveAccountWithZeroBalance() {
    // Act
    final AccountModel account =
        AccountModel.open(
            new AccountId("a-1"),
            new UserId("u-1"),
            new AccountNumber("1234567890"),
            AccountCurrency.COP);

    // Assert
    assertAll(
        () -> assertEquals("a-1", account.getId().value()),
        () -> assertEquals("u-1", account.getUserId().value()),
        () -> assertEquals(AccountBalance.zero(), account.getBalance()),
        () -> assertEquals(AccountCurrency.COP, account.getCurrency()),
        () -> assertEquals(AccountStatus.ACTIVE, account.getStatus()),
        () -> assertEquals(Optional.empty(), account.owner()));
  }

  @Test
  @DisplayName("withOwner() devuelve una copia con correo y nombre del dueno")
  void shouldAttachOwner() {
    // Arrange
    final AccountModel account =
        AccountModel.open(
            new AccountId("a-1"),
            new UserId("u-1"),
            new AccountNumber("1234567890"),
            AccountCurrency.COP);
    final AccountOwner owner =
        new AccountOwner(new UserEmail("john@example.com"), new UserName("John Arrieta"));

    // Act
    final AccountModel withOwner = account.withOwner(owner);

    // Assert
    assertAll(
        () -> assertEquals(Optional.of(owner), withOwner.owner()),
        () -> assertEquals(Optional.empty(), account.owner()));
  }
}
