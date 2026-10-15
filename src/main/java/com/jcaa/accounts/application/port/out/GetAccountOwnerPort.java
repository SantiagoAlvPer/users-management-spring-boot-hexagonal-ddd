package com.jcaa.accounts.application.port.out;

import com.jcaa.accounts.domain.model.AccountOwner;
import com.jcaa.accounts.domain.valueobject.UserId;
import java.util.Optional;

/** Consulta el correo y el nombre de un usuario (tabla de Users). Vacio si el usuario no existe. */
public interface GetAccountOwnerPort {
  Optional<AccountOwner> getOwnerByUserId(UserId userId);
}
