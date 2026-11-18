package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.application.service.dto.query.UserSearchResult;
import java.util.List;

public interface UserSearchIndexPort {

  void indexUser(UserSearchResult document);

  void deleteUser(String userId);

  List<UserSearchResult> search(String query, String status);
}
