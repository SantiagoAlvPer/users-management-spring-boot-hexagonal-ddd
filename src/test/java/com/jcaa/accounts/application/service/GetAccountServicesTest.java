package com.jcaa.accounts.application.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.jcaa.accounts.application.port.out.GetAccountByIdPort;
import com.jcaa.accounts.application.port.out.GetAccountByUserIdPort;
import com.jcaa.accounts.application.service.dto.query.GetAccountByIdQuery;
import com.jcaa.accounts.application.service.dto.query.GetAccountByUserIdQuery;
import com.jcaa.accounts.domain.enums.AccountCurrency;
import com.jcaa.accounts.domain.exception.AccountNotFoundException;
import com.jcaa.accounts.domain.model.AccountModel;
import com.jcaa.accounts.domain.valueobject.AccountId;
import com.jcaa.accounts.domain.valueobject.AccountNumber;
import com.jcaa.accounts.domain.valueobject.UserId;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("GetAccountByIdService / GetAccountByUserIdService")
@ExtendWith(MockitoExtension.class)
class GetAccountServicesTest {

  private static final AccountModel ACCOUNT =
      AccountModel.open(
          new AccountId("a-1"),
          new UserId("u-1"),
          new AccountNumber("1234567890"),
          AccountCurrency.COP);

  @Mock private GetAccountByIdPort getAccountByIdPort;
  @Mock private GetAccountByUserIdPort getAccountByUserIdPort;

  private GetAccountByIdService byIdService;
  private GetAccountByUserIdService byUserIdService;

  @BeforeEach
  void setUp() {
    try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
      byIdService = new GetAccountByIdService(getAccountByIdPort, validatorFactory.getValidator());
      byUserIdService =
          new GetAccountByUserIdService(getAccountByUserIdPort, validatorFactory.getValidator());
    }
  }

  @Test
  @DisplayName("por id: devuelve la cuenta cuando existe")
  void shouldReturnAccountById() {
    // Arrange
    when(getAccountByIdPort.getById(new AccountId("a-1"))).thenReturn(Optional.of(ACCOUNT));

    // Act
    final AccountModel result = byIdService.execute(new GetAccountByIdQuery("a-1"));

    // Assert
    assertSame(ACCOUNT, result);
  }

  @Test
  @DisplayName("por id: lanza AccountNotFoundException cuando no existe")
  void shouldFailWhenIdNotFound() {
    // Arrange
    when(getAccountByIdPort.getById(new AccountId("x"))).thenReturn(Optional.empty());
    final GetAccountByIdQuery query = new GetAccountByIdQuery("x");

    // Act & Assert
    assertThrows(AccountNotFoundException.class, () -> byIdService.execute(query));
  }

  @Test
  @DisplayName("por usuario: devuelve la cuenta cuando existe")
  void shouldReturnAccountByUserId() {
    // Arrange
    when(getAccountByUserIdPort.getByUserId(new UserId("u-1"))).thenReturn(Optional.of(ACCOUNT));

    // Act
    final AccountModel result = byUserIdService.execute(new GetAccountByUserIdQuery("u-1"));

    // Assert
    assertSame(ACCOUNT, result);
  }

  @Test
  @DisplayName("por usuario: lanza AccountNotFoundException cuando no tiene cuenta")
  void shouldFailWhenUserHasNoAccount() {
    // Arrange
    when(getAccountByUserIdPort.getByUserId(new UserId("u-9"))).thenReturn(Optional.empty());
    final GetAccountByUserIdQuery query = new GetAccountByUserIdQuery("u-9");

    // Act & Assert
    assertThrows(AccountNotFoundException.class, () -> byUserIdService.execute(query));
  }
}
