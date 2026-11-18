package com.jcaa.usersmanagement.infrastructure.adapter.persistence.repository;

import com.jcaa.usersmanagement.application.port.out.DeleteUserFilePort;
import com.jcaa.usersmanagement.application.port.out.GetUserFileByKeyPort;
import com.jcaa.usersmanagement.application.port.out.GetUserFilesByUserIdPort;
import com.jcaa.usersmanagement.application.port.out.SaveUserFilePort;
import com.jcaa.usersmanagement.domain.model.UserFileModel;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.exception.PersistenceException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserFileRepositoryPostgreSQL
    implements SaveUserFilePort,
        GetUserFilesByUserIdPort,
        GetUserFileByKeyPort,
        DeleteUserFilePort {

  private static final String SQL_INSERT =
      "INSERT INTO user_files (id, user_id, file_key, content_type, size_bytes, created_at) "
          + "VALUES (?, ?, ?, ?, ?, ?)";

  private static final String SQL_SELECT_BY_USER_ID =
      "SELECT id, user_id, file_key, content_type, size_bytes, created_at "
          + "FROM user_files WHERE user_id = ? ORDER BY created_at DESC";

  private static final String SQL_SELECT_BY_USER_AND_KEY =
      "SELECT id, user_id, file_key, content_type, size_bytes, created_at "
          + "FROM user_files WHERE user_id = ? AND file_key = ? LIMIT 1";

  private static final String SQL_DELETE_BY_USER_AND_KEY =
      "DELETE FROM user_files WHERE user_id = ? AND file_key = ?";

  private final DataSource dataSource;

  @Override
  public UserFileModel save(final UserFileModel userFile) {
    try (final Connection connection = dataSource.getConnection();
        final PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
      statement.setString(1, userFile.getId());
      statement.setString(2, userFile.getUserId());
      statement.setString(3, userFile.getFileKey());
      statement.setString(4, userFile.getContentType());
      statement.setLong(5, userFile.getSizeBytes());
      statement.setTimestamp(6, Timestamp.from(userFile.getCreatedAt()));
      statement.executeUpdate();
      return userFile;
    } catch (final SQLException exception) {
      throw PersistenceException.becauseSaveFailed(userFile.getId(), exception);
    }
  }

  @Override
  public List<UserFileModel> getByUserId(final String userId) {
    try (final Connection connection = dataSource.getConnection();
        final PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_USER_ID)) {
      statement.setString(1, userId);
      final ResultSet resultSet = statement.executeQuery();
      final List<UserFileModel> files = new ArrayList<>();
      while (resultSet.next()) {
        files.add(mapRow(resultSet));
      }
      return files;
    } catch (final SQLException exception) {
      throw PersistenceException.becauseFindByIdFailed(userId, exception);
    }
  }

  @Override
  public Optional<UserFileModel> getByUserIdAndKey(final String userId, final String fileKey) {
    try (final Connection connection = dataSource.getConnection();
        final PreparedStatement statement =
            connection.prepareStatement(SQL_SELECT_BY_USER_AND_KEY)) {
      statement.setString(1, userId);
      statement.setString(2, fileKey);
      final ResultSet resultSet = statement.executeQuery();
      if (!resultSet.next()) {
        return Optional.empty();
      }
      return Optional.of(mapRow(resultSet));
    } catch (final SQLException exception) {
      throw PersistenceException.becauseFindByIdFailed(userId, exception);
    }
  }

  @Override
  public void deleteByUserIdAndKey(final String userId, final String fileKey) {
    try (final Connection connection = dataSource.getConnection();
        final PreparedStatement statement =
            connection.prepareStatement(SQL_DELETE_BY_USER_AND_KEY)) {
      statement.setString(1, userId);
      statement.setString(2, fileKey);
      statement.executeUpdate();
    } catch (final SQLException exception) {
      throw PersistenceException.becauseDeleteFailed(fileKey, exception);
    }
  }

  private UserFileModel mapRow(final ResultSet rs) throws SQLException {
    final Timestamp ts = rs.getTimestamp("created_at");
    final Instant createdAt = ts != null ? ts.toInstant() : Instant.now();
    return new UserFileModel(
        rs.getString("id"),
        rs.getString("user_id"),
        rs.getString("file_key"),
        rs.getString("content_type"),
        rs.getLong("size_bytes"),
        createdAt);
  }
}
