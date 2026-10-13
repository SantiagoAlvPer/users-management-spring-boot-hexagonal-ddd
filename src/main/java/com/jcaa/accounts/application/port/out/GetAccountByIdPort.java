package com.jcaa.accounts.application.port.out;

import com.jcaa.accounts.domain.model.AccountModel;
import com.jcaa.accounts.domain.valueobject.AccountId;
import java.util.Optional;

public interface GetAccountByIdPort {
  Optional<AccountModel> getById(AccountId accountId);
}
