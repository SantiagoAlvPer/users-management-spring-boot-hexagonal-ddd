package com.jcaa.accounts.application.service;

import com.jcaa.accounts.application.port.in.DeleteAccountUseCase;
import com.jcaa.accounts.application.port.out.DeleteAccountPort;
import com.jcaa.accounts.application.port.out.GetAccountByIdPort;
import com.jcaa.accounts.application.service.dto.command.DeleteAccountCommand;
import com.jcaa.accounts.application.service.mapper.AccountApplicationMapper;
import com.jcaa.accounts.domain.exception.AccountNotFoundException;
import com.jcaa.accounts.domain.valueobject.AccountId;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteAccountService implements DeleteAccountUseCase {

  private static final String LOG_DELETED = "[DeleteAccountService] cuenta eliminada. accountId={}";

  private final GetAccountByIdPort getAccountByIdPort;
  private final DeleteAccountPort deleteAccountPort;
  private final Validator validator;

  @Override
  public void execute(final DeleteAccountCommand command) {
    validateCommand(command);
    final AccountId accountId = AccountApplicationMapper.fromDeleteCommandToAccountId(command);
    ensureAccountExists(accountId);
    deleteAccountPort.delete(accountId);
    log.info(LOG_DELETED, accountId.value());
  }

  private void validateCommand(final DeleteAccountCommand command) {
    final Set<ConstraintViolation<DeleteAccountCommand>> violations = validator.validate(command);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }

  private void ensureAccountExists(final AccountId accountId) {
    getAccountByIdPort
        .getById(accountId)
        .orElseThrow(() -> AccountNotFoundException.becauseIdWasNotFound(accountId.value()));
  }
}
