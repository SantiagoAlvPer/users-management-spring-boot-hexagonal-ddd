package com.jcaa.accounts.application.port.out;

import com.jcaa.accounts.domain.model.AccountModel;
import com.jcaa.accounts.domain.valueobject.UserId;
import java.util.Optional;

public interface GetAccountByUserIdPort {
  Optional<AccountModel> getByUserId(UserId userId);
}
