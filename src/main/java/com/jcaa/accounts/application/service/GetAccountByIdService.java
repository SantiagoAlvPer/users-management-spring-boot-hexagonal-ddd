package com.jcaa.accounts.application.service;

import com.jcaa.accounts.application.port.in.GetAccountByIdUseCase;
import com.jcaa.accounts.application.port.out.GetAccountByIdPort;
import com.jcaa.accounts.application.service.dto.query.GetAccountByIdQuery;
import com.jcaa.accounts.application.service.mapper.AccountApplicationMapper;
import com.jcaa.accounts.domain.exception.AccountNotFoundException;
import com.jcaa.accounts.domain.model.AccountModel;
import com.jcaa.accounts.domain.valueobject.AccountId;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAccountByIdService implements GetAccountByIdUseCase {

  private final GetAccountByIdPort getAccountByIdPort;
  private final Validator validator;

  @Override
  public AccountModel execute(final GetAccountByIdQuery query) {
    validateQuery(query);
    final AccountId accountId = AccountApplicationMapper.fromGetByIdQueryToAccountId(query);
    return getAccountByIdPort
        .getById(accountId)
        .orElseThrow(() -> AccountNotFoundException.becauseIdWasNotFound(accountId.value()));
  }

  private void validateQuery(final GetAccountByIdQuery query) {
    final Set<ConstraintViolation<GetAccountByIdQuery>> violations = validator.validate(query);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
