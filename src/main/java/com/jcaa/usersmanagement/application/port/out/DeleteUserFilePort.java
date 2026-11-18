package com.jcaa.usersmanagement.application.port.out;

public interface DeleteUserFilePort {
  void deleteByUserIdAndKey(String userId, String fileKey);
}
