package com.jcaa.accounts.domain.model;

import com.jcaa.accounts.domain.enums.AccountCurrency;
import com.jcaa.accounts.domain.enums.AccountStatus;
import com.jcaa.accounts.domain.valueobject.AccountBalance;
import com.jcaa.accounts.domain.valueobject.AccountId;
import com.jcaa.accounts.domain.valueobject.AccountNumber;
import com.jcaa.accounts.domain.valueobject.UserEmail;
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

  /** Correo del dueno, leido de la tabla de Users. No se guarda en accounts; puede faltar. */
  @Getter(AccessLevel.NONE)
  UserEmail ownerEmail;

  public AccountModel(
      final AccountId id,
      final UserId userId,
      final AccountNumber accountNumber,
      final AccountBalance balance,
      final AccountCurrency currency,
      final AccountStatus status,
      final UserEmail ownerEmail) {
    this.id = id;
    this.userId = userId;
    this.accountNumber = accountNumber;
    this.balance = balance;
    this.currency = currency;
    this.status = status;
    this.ownerEmail = ownerEmail;
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

  public Optional<UserEmail> ownerEmail() {
    return Optional.ofNullable(ownerEmail);
  }

  public AccountModel withOwnerEmail(final UserEmail email) {
    return new AccountModel(id, userId, accountNumber, balance, currency, status, email);
  }
}
