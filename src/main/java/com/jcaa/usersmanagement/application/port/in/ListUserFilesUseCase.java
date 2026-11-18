package com.jcaa.usersmanagement.application.port.in;

import com.jcaa.usersmanagement.domain.model.UserFileModel;
import java.util.List;

public interface ListUserFilesUseCase {
  List<UserFileModel> execute(String userId);
}
