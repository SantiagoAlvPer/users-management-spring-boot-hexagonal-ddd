package com.jcaa.notifiers.application.port.in;

import com.jcaa.notifiers.application.service.dto.UserCreatedNotificationCommand;
import com.jcaa.notifiers.application.service.dto.UserUpdatedNotificationCommand;

public interface SendNotificationUseCase {

  void notifyUserCreated(UserCreatedNotificationCommand command);

  void notifyUserUpdated(UserUpdatedNotificationCommand command);
}
