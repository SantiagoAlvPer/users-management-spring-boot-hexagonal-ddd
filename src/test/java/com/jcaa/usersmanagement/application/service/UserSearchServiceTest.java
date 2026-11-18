package com.jcaa.usersmanagement.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.application.port.out.GetAllUsersPort;
import com.jcaa.usersmanagement.application.port.out.UserSearchIndexPort;
import com.jcaa.usersmanagement.application.service.dto.query.SearchUsersQuery;
import com.jcaa.usersmanagement.application.service.dto.query.UserSearchResult;
import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserSearchServiceTest {

  @Mock private UserSearchIndexPort userSearchIndexPort;
  @Mock private GetAllUsersPort getAllUsersPort;

  private UserSearchService service;

  @BeforeEach
  void setUp() {
    service = new UserSearchService(userSearchIndexPort, getAllUsersPort);
  }

  @Test
  void shouldDelegateSearchToIndexPort() {
    // Arrange
    final List<UserSearchResult> expected =
        List.of(new UserSearchResult("u1", "Ana", "ana@example.com", "MEMBER", "ACTIVE"));
    when(userSearchIndexPort.search("Ana", "ACTIVE")).thenReturn(expected);

    // Act
    final List<UserSearchResult> result = service.execute(new SearchUsersQuery("Ana", "ACTIVE"));

    // Assert
    assertThat(result).isEqualTo(expected);
  }

  @Test
  void shouldReindexAllUsersFromDatabase() {
    // Arrange
    final UserModel user =
        new UserModel(
            new UserId("u1"),
            new UserName("Ana"),
            new UserEmail("ana@example.com"),
            UserPassword.fromHash("$2a$12$abcdefghijklmnopqrstuvwxyz01234567890123456789012345"),
            UserRole.MEMBER,
            UserStatus.ACTIVE);
    when(getAllUsersPort.getAll()).thenReturn(List.of(user));

    // Act
    final int count = service.execute();

    // Assert
    assertThat(count).isEqualTo(1);
    verify(userSearchIndexPort, times(1)).indexUser(any(UserSearchResult.class));
  }
}
