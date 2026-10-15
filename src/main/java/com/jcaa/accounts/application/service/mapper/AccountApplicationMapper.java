package com.jcaa.accounts.application.service.mapper;

import com.jcaa.accounts.application.service.dto.command.CreateAccountCommand;
import com.jcaa.accounts.application.service.dto.command.DeleteAccountCommand;
import com.jcaa.accounts.application.service.dto.query.GetAccountByIdQuery;
import com.jcaa.accounts.application.service.dto.query.GetAccountByUserIdQuery;
import com.jcaa.accounts.domain.enums.AccountCurrency;
import com.jcaa.accounts.domain.model.AccountModel;
import com.jcaa.accounts.domain.valueobject.AccountId;
import com.jcaa.accounts.domain.valueobject.AccountNumber;
import com.jcaa.accounts.domain.valueobject.UserId;
import java.util.Objects;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AccountApplicationMapper {

  public AccountModel fromCreateCommandToModel(final CreateAccountCommand command) {
    return AccountModel.open(
        AccountId.generate(),
        new UserId(command.userId()),
        AccountNumber.generate(),
        resolveCurrency(command.currency()));
  }

  public UserId fromCreateCommandToUserId(final CreateAccountCommand command) {
    return new UserId(command.userId());
  }

  public AccountId fromGetByIdQueryToAccountId(final GetAccountByIdQuery query) {
    return new AccountId(query.id());
  }

  public AccountId fromDeleteCommandToAccountId(final DeleteAccountCommand command) {
    return new AccountId(command.id());
  }

  public UserId fromGetByUserIdQueryToUserId(final GetAccountByUserIdQuery query) {
    return new UserId(query.userId());
  }

  private AccountCurrency resolveCurrency(final String currency) {
    if (Objects.isNull(currency) || currency.isBlank()) {
      return AccountCurrency.defaultCurrency();
    }
    return AccountCurrency.fromString(currency);
  }
}
