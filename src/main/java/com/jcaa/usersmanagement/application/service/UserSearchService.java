package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.ReindexUsersUseCase;
import com.jcaa.usersmanagement.application.port.in.SearchUsersUseCase;
import com.jcaa.usersmanagement.application.port.out.GetAllUsersPort;
import com.jcaa.usersmanagement.application.port.out.UserSearchIndexPort;
import com.jcaa.usersmanagement.application.service.dto.query.SearchUsersQuery;
import com.jcaa.usersmanagement.application.service.dto.query.UserSearchResult;
import com.jcaa.usersmanagement.domain.model.UserModel;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSearchService implements SearchUsersUseCase, ReindexUsersUseCase {

  private final UserSearchIndexPort userSearchIndexPort;
  private final GetAllUsersPort getAllUsersPort;

  @Override
  public List<UserSearchResult> execute(final SearchUsersQuery query) {
    final String q = query == null ? "" : query.query();
    final String status = query == null ? null : query.status();
    return userSearchIndexPort.search(q, status);
  }

  @Override
  public int execute() {
    final List<UserModel> users = getAllUsersPort.getAll();
    for (final UserModel user : users) {
      userSearchIndexPort.indexUser(
          new UserSearchResult(
              user.getId().value(),
              user.getName().value(),
              user.getEmail().value(),
              user.getRole().name(),
              user.getStatus().name()));
    }
    return users.size();
  }
}
