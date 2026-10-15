package com.jcaa.accounts.application.service;

import com.jcaa.accounts.application.port.in.CreateAccountUseCase;
import com.jcaa.accounts.application.port.out.GetAccountByUserIdPort;
import com.jcaa.accounts.application.port.out.GetUserEmailPort;
import com.jcaa.accounts.application.port.out.PublishAccountCreatedEventPort;
import com.jcaa.accounts.application.port.out.SaveAccountPort;
import com.jcaa.accounts.application.service.dto.command.CreateAccountCommand;
import com.jcaa.accounts.application.service.mapper.AccountApplicationMapper;
import com.jcaa.accounts.domain.event.AccountCreatedDomainEvent;
import com.jcaa.accounts.domain.exception.AccountAlreadyExistsException;
import com.jcaa.accounts.domain.exception.UserNotFoundException;
import com.jcaa.accounts.domain.model.AccountModel;
import com.jcaa.accounts.domain.valueobject.UserEmail;
import com.jcaa.accounts.domain.valueobject.UserId;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Paso 5-7 del flujo: Users pide crear la cuenta (sync), se consulta el correo del usuario en Users
 * (sync), se guarda en PostgreSQL (sync) y se publica {@code account.created} en Kafka (async).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateAccountService implements CreateAccountUseCase {

  private static final String LOG_CREATED = "[CreateAccountService] cuenta creada. accountId={}";

  private final SaveAccountPort saveAccountPort;
  private final GetAccountByUserIdPort getAccountByUserIdPort;
  private final GetUserEmailPort getUserEmailPort;
  private final PublishAccountCreatedEventPort publishAccountCreatedEventPort;
  private final Validator validator;

  @Override
  public AccountModel execute(final CreateAccountCommand command) {
    validateCommand(command);

    final UserId userId = AccountApplicationMapper.fromCreateCommandToUserId(command);
    ensureUserHasNoAccount(userId);
    final AccountModel accountToSave = AccountApplicationMapper.fromCreateCommandToModel(command);
    // Antes de guardar: si el usuario no existe o Users falla, no queda una cuenta huerfana
    final UserEmail userEmail = findUserEmail(userId);

    final AccountModel savedAccount = saveAccountPort.save(accountToSave);
    log.info(LOG_CREATED, savedAccount.getId().value());

    publishAccountCreatedEventPort.publish(new AccountCreatedDomainEvent(savedAccount, userEmail));
    return savedAccount;
  }

  private void validateCommand(final CreateAccountCommand command) {
    final Set<ConstraintViolation<CreateAccountCommand>> violations = validator.validate(command);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }

  private UserEmail findUserEmail(final UserId userId) {
    return getUserEmailPort
        .getEmailByUserId(userId)
        .orElseThrow(() -> UserNotFoundException.becauseIdWasNotFound(userId.value()));
  }

  private void ensureUserHasNoAccount(final UserId userId) {
    getAccountByUserIdPort
        .getByUserId(userId)
        .ifPresent(
            ignored -> {
              throw AccountAlreadyExistsException.becauseUserAlreadyHasAccount(userId.value());
            });
  }
}
