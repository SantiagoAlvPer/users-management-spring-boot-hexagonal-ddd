package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.model.UserFileModel;

public interface SaveUserFilePort {
  UserFileModel save(UserFileModel userFile);
}
