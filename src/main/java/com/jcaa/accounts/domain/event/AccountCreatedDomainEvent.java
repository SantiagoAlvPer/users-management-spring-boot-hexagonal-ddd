package com.jcaa.accounts.domain.event;

import com.jcaa.accounts.domain.model.AccountModel;
import java.util.Map;
import lombok.Getter;

@Getter
public final class AccountCreatedDomainEvent extends DomainEvent {

  private static final String EVENT_NAME = "account.created";

  private final AccountModel account;

  public AccountCreatedDomainEvent(final AccountModel account) {
    super(EVENT_NAME);
    this.account = account;
  }

  @Override
  public Map<String, String> payload() {
    return Map.of(
        "id", account.getId().value(),
        "userId", account.getUserId().value(),
        "accountNumber", account.getAccountNumber().value(),
        "balance", account.getBalance().toString(),
        "currency", account.getCurrency().name(),
        "status", account.getStatus().name());
  }
}
