package com.jcaa.accounts.domain.model;

import com.jcaa.accounts.domain.valueobject.UserEmail;
import com.jcaa.accounts.domain.valueobject.UserName;
import java.util.Objects;

/** Datos del dueno de la cuenta que vienen de la tabla de Users (no se guardan en accounts). */
public record AccountOwner(UserEmail email, UserName name) {

  public AccountOwner {
    Objects.requireNonNull(email, "AccountOwner email cannot be null");
    Objects.requireNonNull(name, "AccountOwner name cannot be null");
  }
}
