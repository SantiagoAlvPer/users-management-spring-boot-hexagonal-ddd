package com.jcaa.accounts.application.port.out;

import com.jcaa.accounts.domain.valueobject.UserEmail;
import com.jcaa.accounts.domain.valueobject.UserId;
import java.util.Optional;

/** Consulta el correo de un usuario (tabla de Users). Vacio si el usuario no existe. */
public interface GetUserEmailPort {
  Optional<UserEmail> getEmailByUserId(UserId userId);
}
