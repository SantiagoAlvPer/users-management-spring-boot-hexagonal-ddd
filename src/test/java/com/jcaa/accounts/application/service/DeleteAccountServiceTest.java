package com.jcaa.accounts.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcaa.accounts.application.port.out.DeleteAccountPort;
import com.jcaa.accounts.application.port.out.GetAccountByIdPort;
import com.jcaa.accounts.application.service.dto.command.DeleteAccountCommand;
import com.jcaa.accounts.domain.enums.AccountCurrency;
import com.jcaa.accounts.domain.exception.AccountNotFoundException;
import com.jcaa.accounts.domain.model.AccountModel;
import com.jcaa.accounts.domain.valueobject.AccountId;
import com.jcaa.accounts.domain.valueobject.AccountNumber;
import com.jcaa.accounts.domain.valueobject.UserId;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("DeleteAccountService")
@ExtendWith(MockitoExtension.class)
class DeleteAccountServiceTest {

  @Mock private GetAccountByIdPort getAccountByIdPort;
  @Mock private DeleteAccountPort deleteAccountPort;

  private DeleteAccountService service;

  @BeforeEach
  void setUp() {
    try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
      service =
          new DeleteAccountService(
              getAccountByIdPort, deleteAccountPort, validatorFactory.getValidator());
    }
  }

  @Test
  @DisplayName("execute() elimina la cuenta cuando existe")
  void shouldDeleteWhenAccountExists() {
    // Arrange
    final AccountModel account =
        AccountModel.open(
            new AccountId("a-1"),
            new UserId("u-1"),
            new AccountNumber("1234567890"),
            AccountCurrency.COP);
    when(getAccountByIdPort.getById(new AccountId("a-1"))).thenReturn(Optional.of(account));

    // Act
    service.execute(new DeleteAccountCommand("a-1"));

    // Assert
    verify(deleteAccountPort).delete(new AccountId("a-1"));
  }

  @Test
  @DisplayName("execute() lanza AccountNotFoundException si la cuenta no existe")
  void shouldFailWhenAccountDoesNotExist() {
    // Arrange
    when(getAccountByIdPort.getById(new AccountId("x"))).thenReturn(Optional.empty());
    final DeleteAccountCommand command = new DeleteAccountCommand("x");

    // Act & Assert
    assertThrows(AccountNotFoundException.class, () -> service.execute(command));
    verify(deleteAccountPort, never()).delete(any());
  }

  @Test
  @DisplayName("execute() lanza ConstraintViolationException si el id esta vacio")
  void shouldFailWhenIdIsBlank() {
    // Arrange
    final DeleteAccountCommand command = new DeleteAccountCommand(" ");

    // Act & Assert
    assertThrows(ConstraintViolationException.class, () -> service.execute(command));
    verify(deleteAccountPort, never()).delete(any());
  }
}
