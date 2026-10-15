package com.jcaa.accounts.domain.model;

import com.jcaa.accounts.domain.enums.AccountCurrency;
import com.jcaa.accounts.domain.enums.AccountStatus;
import com.jcaa.accounts.domain.valueobject.AccountBalance;
import com.jcaa.accounts.domain.valueobject.AccountId;
import com.jcaa.accounts.domain.valueobject.AccountNumber;
import com.jcaa.accounts.domain.valueobject.UserId;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
public class AccountModel {

  AccountId id;
  UserId userId;
  AccountNumber accountNumber;
  AccountBalance balance;
  AccountCurrency currency;
  AccountStatus status;

  /** Correo y nombre del dueno, leidos de la tabla de Users. No se guardan; pueden faltar. */
  @Getter(AccessLevel.NONE)
  AccountOwner owner;

  public AccountModel(
      final AccountId id,
      final UserId userId,
      final AccountNumber accountNumber,
      final AccountBalance balance,
      final AccountCurrency currency,
      final AccountStatus status,
      final AccountOwner owner) {
    this.id = id;
    this.userId = userId;
    this.accountNumber = accountNumber;
    this.balance = balance;
    this.currency = currency;
    this.status = status;
    this.owner = owner;
  }

  /** Abre una cuenta nueva: saldo en cero y estado ACTIVE. */
  public static AccountModel open(
      final AccountId id,
      final UserId userId,
      final AccountNumber accountNumber,
      final AccountCurrency currency) {
    return new AccountModel(
        id, userId, accountNumber, AccountBalance.zero(), currency, AccountStatus.ACTIVE, null);
  }

  public Optional<AccountOwner> owner() {
    return Optional.ofNullable(owner);
  }

  public AccountModel withOwner(final AccountOwner newOwner) {
    return new AccountModel(id, userId, accountNumber, balance, currency, status, newOwner);
  }
}
