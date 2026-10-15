package com.jcaa.accounts.application.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.jcaa.accounts.application.port.out.GetAccountByUserIdPort;
import com.jcaa.accounts.application.port.out.GetUserEmailPort;
import com.jcaa.accounts.application.port.out.PublishAccountCreatedEventPort;
import com.jcaa.accounts.application.port.out.SaveAccountPort;
import com.jcaa.accounts.application.service.dto.command.CreateAccountCommand;
import com.jcaa.accounts.domain.enums.AccountCurrency;
import com.jcaa.accounts.domain.enums.AccountStatus;
import com.jcaa.accounts.domain.event.AccountCreatedDomainEvent;
import com.jcaa.accounts.domain.exception.AccountAlreadyExistsException;
import com.jcaa.accounts.domain.exception.InvalidAccountCurrencyException;
import com.jcaa.accounts.domain.exception.UserNotFoundException;
import com.jcaa.accounts.domain.model.AccountModel;
import com.jcaa.accounts.domain.valueobject.AccountId;
import com.jcaa.accounts.domain.valueobject.AccountNumber;
import com.jcaa.accounts.domain.valueobject.UserEmail;
import com.jcaa.accounts.domain.valueobject.UserId;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("CreateAccountService")
@ExtendWith(MockitoExtension.class)
class CreateAccountServiceTest {

  private static final UserEmail EMAIL = new UserEmail("john@example.com");

  @Mock private SaveAccountPort saveAccountPort;
  @Mock private GetAccountByUserIdPort getAccountByUserIdPort;
  @Mock private GetUserEmailPort getUserEmailPort;
  @Mock private PublishAccountCreatedEventPort publishAccountCreatedEventPort;

  private CreateAccountService service;

  @BeforeEach
  void setUp() {
    try (final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
      service =
          new CreateAccountService(
              saveAccountPort,
              getAccountByUserIdPort,
              getUserEmailPort,
              publishAccountCreatedEventPort,
              validatorFactory.getValidator());
    }
  }

  @Test
  @DisplayName("execute() guarda la cuenta ACTIVE en COP y publica account.created con el email")
  void shouldSaveAndPublishWhenUserHasNoAccount() {
    // Arrange
    final CreateAccountCommand command = new CreateAccountCommand("u-1", null);
    when(getAccountByUserIdPort.getByUserId(new UserId("u-1"))).thenReturn(Optional.empty());
    when(getUserEmailPort.getEmailByUserId(new UserId("u-1"))).thenReturn(Optional.of(EMAIL));
    when(saveAccountPort.save(any(AccountModel.class))).thenAnswer(inv -> inv.getArgument(0));

    // Act
    final AccountModel result = service.execute(command);

    // Assert
    final ArgumentCaptor<AccountCreatedDomainEvent> eventCaptor =
        ArgumentCaptor.forClass(AccountCreatedDomainEvent.class);
    verify(publishAccountCreatedEventPort).publish(eventCaptor.capture());
    assertAll(
        () -> assertEquals("u-1", result.getUserId().value()),
        () -> assertEquals(AccountCurrency.COP, result.getCurrency()),
        () -> assertEquals(AccountStatus.ACTIVE, result.getStatus()),
        () -> assertSame(result, eventCaptor.getValue().getAccount()),
        () -> assertEquals("john@example.com", eventCaptor.getValue().payload().get("email")),
        () -> assertEquals(Optional.of(EMAIL), result.ownerEmail()));
  }

  @Test
  @DisplayName("execute() usa la moneda enviada")
  void shouldUseRequestedCurrency() {
    // Arrange
    final CreateAccountCommand command = new CreateAccountCommand("u-1", "usd");
    when(getAccountByUserIdPort.getByUserId(any())).thenReturn(Optional.empty());
    when(getUserEmailPort.getEmailByUserId(any())).thenReturn(Optional.of(EMAIL));
    when(saveAccountPort.save(any(AccountModel.class))).thenAnswer(inv -> inv.getArgument(0));

    // Act
    final AccountModel result = service.execute(command);

    // Assert
    assertEquals(AccountCurrency.USD, result.getCurrency());
  }

  @Test
  @DisplayName("execute() lanza UserNotFoundException si Users no conoce al usuario, sin guardar")
  void shouldFailWhenUserDoesNotExist() {
    // Arrange
    final CreateAccountCommand command = new CreateAccountCommand("u-9", null);
    when(getAccountByUserIdPort.getByUserId(any())).thenReturn(Optional.empty());
    when(getUserEmailPort.getEmailByUserId(new UserId("u-9"))).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UserNotFoundException.class, () -> service.execute(command));
    verify(saveAccountPort, never()).save(any());
    verify(publishAccountCreatedEventPort, never()).publish(any());
  }

  @Test
  @DisplayName("execute() lanza AccountAlreadyExistsException si el usuario ya tiene cuenta")
  void shouldFailWhenUserAlreadyHasAccount() {
    // Arrange
    final CreateAccountCommand command = new CreateAccountCommand("u-1", "COP");
    final AccountModel existing =
        AccountModel.open(
            new AccountId("a-1"),
            new UserId("u-1"),
            new AccountNumber("1234567890"),
            AccountCurrency.COP);
    when(getAccountByUserIdPort.getByUserId(new UserId("u-1"))).thenReturn(Optional.of(existing));

    // Act & Assert
    assertThrows(AccountAlreadyExistsException.class, () -> service.execute(command));
    verifyNoInteractions(getUserEmailPort);
    verify(saveAccountPort, never()).save(any());
    verify(publishAccountCreatedEventPort, never()).publish(any());
  }

  @Test
  @DisplayName("execute() lanza ConstraintViolationException si userId esta vacio")
  void shouldFailWhenUserIdIsBlank() {
    // Arrange
    final CreateAccountCommand command = new CreateAccountCommand(" ", null);

    // Act & Assert
    assertThrows(ConstraintViolationException.class, () -> service.execute(command));
    verify(saveAccountPort, never()).save(any());
  }

  @Test
  @DisplayName("execute() rechaza monedas no soportadas sin consultar Users ni guardar")
  void shouldFailWhenCurrencyIsUnsupported() {
    // Arrange
    final CreateAccountCommand command = new CreateAccountCommand("u-1", "EUR");
    when(getAccountByUserIdPort.getByUserId(any())).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(InvalidAccountCurrencyException.class, () -> service.execute(command));
    verifyNoInteractions(getUserEmailPort);
    verify(saveAccountPort, never()).save(any());
  }
}
