package com.jcaa.accounts.domain.event;

import com.jcaa.accounts.domain.model.AccountModel;
import com.jcaa.accounts.domain.valueobject.UserEmail;
import java.util.Map;
import lombok.Getter;

@Getter
public final class AccountCreatedDomainEvent extends DomainEvent {

  private static final String EVENT_NAME = "account.created";

  private final AccountModel account;
  private final UserEmail userEmail;

  public AccountCreatedDomainEvent(final AccountModel account, final UserEmail userEmail) {
    super(EVENT_NAME);
    this.account = account;
    this.userEmail = userEmail;
  }

  @Override
  public Map<String, String> payload() {
    return Map.of(
        "id", account.getId().value(),
        "userId", account.getUserId().value(),
        "email", userEmail.value(),
        "accountNumber", account.getAccountNumber().value(),
        "balance", account.getBalance().toString(),
        "currency", account.getCurrency().name(),
        "status", account.getStatus().name());
  }
}
