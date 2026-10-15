package com.jcaa.accounts.infrastructure.adapter.persistence.repository;

import com.jcaa.accounts.application.port.out.GetUserEmailPort;
import com.jcaa.accounts.domain.valueobject.UserEmail;
import com.jcaa.accounts.domain.valueobject.UserId;
import com.jcaa.accounts.infrastructure.adapter.persistence.config.UsersTableName;
import com.jcaa.accounts.infrastructure.adapter.persistence.exception.PersistenceException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * Lee el correo del usuario directamente de la tabla de Users (misma base PostgreSQL). Solo lectura:
 * accounts nunca escribe en esa tabla.
 */
@Repository
public class UserEmailRepositoryPostgres implements GetUserEmailPort {

  // id::text funciona si la columna id es VARCHAR o UUID
  private static final String SQL_SELECT_EMAIL = "SELECT email FROM %s WHERE id::text = ? LIMIT 1";
  private static final String COL_EMAIL = "email";

  private final DataSource dataSource;
  private final String selectEmailSql;

  public UserEmailRepositoryPostgres(
      final DataSource dataSource, @Value("${users.table-name}") final String tableName) {
    this.dataSource = dataSource;
    this.selectEmailSql = String.format(SQL_SELECT_EMAIL, UsersTableName.requireValid(tableName));
  }

  @Override
  public Optional<UserEmail> getEmailByUserId(final UserId userId) {
    try (final Connection connection = dataSource.getConnection();
        final PreparedStatement statement = connection.prepareStatement(selectEmailSql)) {
      statement.setString(1, userId.value());
      try (final ResultSet resultSet = statement.executeQuery()) {
        if (!resultSet.next()) {
          return Optional.empty();
        }
        return Optional.of(new UserEmail(resultSet.getString(COL_EMAIL)));
      }
    } catch (final SQLException exception) {
      throw PersistenceException.becauseFindUserEmailFailed(userId.value(), exception);
    }
  }
}
