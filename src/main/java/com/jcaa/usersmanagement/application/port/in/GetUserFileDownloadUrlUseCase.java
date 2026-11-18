package com.jcaa.usersmanagement.application.port.in;

import com.jcaa.usersmanagement.application.service.dto.query.GetUserFileDownloadUrlQuery;
import com.jcaa.usersmanagement.application.service.dto.query.PresignedDownloadUrl;

public interface GetUserFileDownloadUrlUseCase {
  PresignedDownloadUrl execute(GetUserFileDownloadUrlQuery query);
}
