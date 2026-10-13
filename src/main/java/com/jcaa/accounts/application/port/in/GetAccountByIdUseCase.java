package com.jcaa.accounts.application.port.in;

import com.jcaa.accounts.application.service.dto.query.GetAccountByIdQuery;
import com.jcaa.accounts.domain.model.AccountModel;

public interface GetAccountByIdUseCase {
  AccountModel execute(GetAccountByIdQuery query);
}
