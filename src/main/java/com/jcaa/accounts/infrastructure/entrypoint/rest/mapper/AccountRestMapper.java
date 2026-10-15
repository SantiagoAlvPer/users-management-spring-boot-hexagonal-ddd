package com.jcaa.accounts.infrastructure.entrypoint.rest.mapper;

import com.jcaa.accounts.application.service.dto.command.CreateAccountCommand;
import com.jcaa.accounts.application.service.dto.command.DeleteAccountCommand;
import com.jcaa.accounts.application.service.dto.query.GetAccountByIdQuery;
import com.jcaa.accounts.application.service.dto.query.GetAccountByUserIdQuery;
import com.jcaa.accounts.domain.model.AccountModel;
import com.jcaa.accounts.infrastructure.entrypoint.rest.dto.request.CreateAccountRestRequest;
import com.jcaa.accounts.infrastructure.entrypoint.rest.dto.response.AccountRestResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AccountRestMapper {

  public CreateAccountCommand toCreateCommand(final CreateAccountRestRequest request) {
    return new CreateAccountCommand(request.userId(), request.currency());
  }

  public GetAccountByIdQuery toGetByIdQuery(final String id) {
    return new GetAccountByIdQuery(id);
  }

  public DeleteAccountCommand toDeleteCommand(final String id) {
    return new DeleteAccountCommand(id);
  }

  public GetAccountByUserIdQuery toGetByUserIdQuery(final String userId) {
    return new GetAccountByUserIdQuery(userId);
  }

  public AccountRestResponse toResponse(final AccountModel account) {
    return new AccountRestResponse(
        account.getId().value(),
        account.getUserId().value(),
        account.getAccountNumber().value(),
        account.getBalance().value(),
        account.getCurrency().name(),
        account.getStatus().name());
  }
}
