package com.jcaa.accounts.application.port.in;

import com.jcaa.accounts.application.service.dto.query.GetAccountByUserIdQuery;
import com.jcaa.accounts.domain.model.AccountModel;

public interface GetAccountByUserIdUseCase {
  AccountModel execute(GetAccountByUserIdQuery query);
}
