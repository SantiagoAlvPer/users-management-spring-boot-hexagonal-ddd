package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.model.UserFileModel;
import java.util.Optional;

public interface GetUserFileByKeyPort {
  Optional<UserFileModel> getByUserIdAndKey(String userId, String fileKey);
}
