package com.jcaa.accounts.application.port.out;

import com.jcaa.accounts.domain.model.AccountModel;

public interface SaveAccountPort {
  AccountModel save(AccountModel account);
}
