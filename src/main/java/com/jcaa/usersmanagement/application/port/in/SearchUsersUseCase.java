package com.jcaa.usersmanagement.application.port.in;

import com.jcaa.usersmanagement.application.service.dto.query.SearchUsersQuery;
import com.jcaa.usersmanagement.application.service.dto.query.UserSearchResult;
import java.util.List;

public interface SearchUsersUseCase {
  List<UserSearchResult> execute(SearchUsersQuery query);
}
