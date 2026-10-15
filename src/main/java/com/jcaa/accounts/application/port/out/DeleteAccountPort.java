package com.jcaa.accounts.application.port.out;

import com.jcaa.accounts.domain.valueobject.AccountId;

public interface DeleteAccountPort {
  void delete(AccountId accountId);
}
