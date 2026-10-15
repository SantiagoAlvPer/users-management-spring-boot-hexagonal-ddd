package com.jcaa.accounts.domain.event;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.jcaa.accounts.domain.enums.AccountCurrency;
import com.jcaa.accounts.domain.model.AccountModel;
import com.jcaa.accounts.domain.model.AccountOwner;
import com.jcaa.accounts.domain.valueobject.AccountId;
import com.jcaa.accounts.domain.valueobject.AccountNumber;
import com.jcaa.accounts.domain.valueobject.UserEmail;
import com.jcaa.accounts.domain.valueobject.UserId;
import com.jcaa.accounts.domain.valueobject.UserName;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AccountCreatedDomainEvent")
class AccountCreatedDomainEventTest {

  @Test
  @DisplayName("payload() expone los datos de la cuenta, email y nombre con el nombre account.created")
  void shouldBuildPayload() {
    // Arrange
    final AccountModel account =
        AccountModel.open(
            new AccountId("a-1"),
            new UserId("u-1"),
            new AccountNumber("1234567890"),
            AccountCurrency.COP);

    // Act
    final AccountCreatedDomainEvent event =
        new AccountCreatedDomainEvent(
            account,
            new AccountOwner(new UserEmail("john@example.com"), new UserName("John Arrieta")));
    final Map<String, String> payload = event.payload();

    // Assert
    assertAll(
        () -> assertEquals("account.created", event.getEventName()),
        () -> assertNotNull(event.getOccurredOn()),
        () -> assertEquals("a-1", payload.get("id")),
        () -> assertEquals("u-1", payload.get("userId")),
        () -> assertEquals("john@example.com", payload.get("email")),
        () -> assertEquals("John Arrieta", payload.get("name")),
        () -> assertEquals("1234567890", payload.get("accountNumber")),
        () -> assertEquals("0.00", payload.get("balance")),
        () -> assertEquals("COP", payload.get("currency")),
        () -> assertEquals("ACTIVE", payload.get("status")));
  }
}
