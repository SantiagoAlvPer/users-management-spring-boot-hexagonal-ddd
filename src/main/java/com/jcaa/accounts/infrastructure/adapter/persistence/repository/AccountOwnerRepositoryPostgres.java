package com.jcaa.accounts.infrastructure.adapter.persistence.repository;

import com.jcaa.accounts.application.port.out.GetAccountOwnerPort;
import com.jcaa.accounts.domain.model.AccountOwner;
import com.jcaa.accounts.domain.valueobject.UserEmail;
import com.jcaa.accounts.domain.valueobject.UserId;
import com.jcaa.accounts.domain.valueobject.UserName;
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
 * Lee correo y nombre del usuario directamente de la tabla de Users (misma base PostgreSQL). Solo
 * lectura: accounts nunca escribe en esa tabla.
 */
@Repository
public class AccountOwnerRepositoryPostgres implements GetAccountOwnerPort {

  // id::text funciona si la columna id es VARCHAR o UUID
  private static final String SQL_SELECT_OWNER =
      "SELECT email, name FROM %s WHERE id::text = ? LIMIT 1";
  private static final String COL_EMAIL = "email";
  private static final String COL_NAME = "name";

  private final DataSource dataSource;
  private final String selectOwnerSql;

  public AccountOwnerRepositoryPostgres(
      final DataSource dataSource, @Value("${users.table-name}") final String tableName) {
    this.dataSource = dataSource;
    this.selectOwnerSql = String.format(SQL_SELECT_OWNER, UsersTableName.requireValid(tableName));
  }

  @Override
  public Optional<AccountOwner> getOwnerByUserId(final UserId userId) {
    try (final Connection connection = dataSource.getConnection();
        final PreparedStatement statement = connection.prepareStatement(selectOwnerSql)) {
      statement.setString(1, userId.value());
      try (final ResultSet resultSet = statement.executeQuery()) {
        if (!resultSet.next()) {
          return Optional.empty();
        }
        return Optional.of(
            new AccountOwner(
                new UserEmail(resultSet.getString(COL_EMAIL)),
                new UserName(resultSet.getString(COL_NAME))));
      }
    } catch (final SQLException exception) {
      throw PersistenceException.becauseFindUserEmailFailed(userId.value(), exception);
    }
  }
}
