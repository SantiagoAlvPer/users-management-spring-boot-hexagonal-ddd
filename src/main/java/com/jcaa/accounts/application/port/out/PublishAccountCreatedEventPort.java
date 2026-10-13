package com.jcaa.accounts.application.port.out;

import com.jcaa.accounts.domain.event.AccountCreatedDomainEvent;

/** Publica (de forma asincrona) el evento de cuenta creada. */
public interface PublishAccountCreatedEventPort {
  void publish(AccountCreatedDomainEvent event);
}
