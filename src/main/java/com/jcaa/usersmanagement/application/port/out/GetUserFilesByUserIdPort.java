package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.model.UserFileModel;
import java.util.List;

public interface GetUserFilesByUserIdPort {
  List<UserFileModel> getByUserId(String userId);
}
