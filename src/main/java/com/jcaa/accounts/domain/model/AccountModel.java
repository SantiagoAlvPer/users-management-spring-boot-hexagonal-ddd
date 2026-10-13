package com.jcaa.accounts.domain.model;

import com.jcaa.accounts.domain.enums.AccountCurrency;
import com.jcaa.accounts.domain.enums.AccountStatus;
import com.jcaa.accounts.domain.valueobject.AccountBalance;
import com.jcaa.accounts.domain.valueobject.AccountId;
import com.jcaa.accounts.domain.valueobject.AccountNumber;
import com.jcaa.accounts.domain.valueobject.UserId;
import lombok.Value;

@Value
public class AccountModel {

  AccountId id;
  UserId userId;
  AccountNumber accountNumber;
  AccountBalance balance;
  AccountCurrency currency;
  AccountStatus status;

  /** Abre una cuenta nueva: saldo en cero y estado ACTIVE. */
  public static AccountModel open(
      final AccountId id,
      final UserId userId,
      final AccountNumber accountNumber,
      final AccountCurrency currency) {
    return new AccountModel(
        id, userId, accountNumber, AccountBalance.zero(), currency, AccountStatus.ACTIVE);
  }
}
