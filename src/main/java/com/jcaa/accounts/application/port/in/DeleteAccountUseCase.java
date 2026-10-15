package com.jcaa.accounts.application.port.in;

import com.jcaa.accounts.application.service.dto.command.DeleteAccountCommand;

public interface DeleteAccountUseCase {
  void execute(DeleteAccountCommand command);
}
