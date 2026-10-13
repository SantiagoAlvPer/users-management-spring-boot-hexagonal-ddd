package com.jcaa.accounts.infrastructure.adapter.persistence.repository;

import com.jcaa.accounts.application.port.out.GetAccountByIdPort;
import com.jcaa.accounts.application.port.out.GetAccountByUserIdPort;
import com.jcaa.accounts.application.port.out.SaveAccountPort;
import com.jcaa.accounts.domain.exception.AccountAlreadyExistsException;
import com.jcaa.accounts.domain.exception.AccountNotFoundException;
import com.jcaa.accounts.domain.model.AccountModel;
import com.jcaa.accounts.domain.valueobject.AccountId;
import com.jcaa.accounts.domain.valueobject.UserId;
import com.jcaa.accounts.infrastructure.adapter.persistence.dto.AccountPersistenceDto;
import com.jcaa.accounts.infrastructure.adapter.persistence.exception.PersistenceException;
import com.jcaa.accounts.infrastructure.adapter.persistence.mapper.AccountPersistenceMapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** Adaptador JDBC para PostgreSQL. Implementa todos los puertos de salida de persistencia. */
@Repository
@RequiredArgsConstructor
public class AccountRepositoryPostgres
    implements SaveAccountPort, GetAccountByIdPort, GetAccountByUserIdPort {

  private static final String SQL_INSERT =
      "INSERT INTO accounts "
          + "(id, user_id, account_number, balance, currency, status, created_at, updated_at) "
          + "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";

  private static final String SQL_SELECT_BY_ID =
      "SELECT id, user_id, account_number, balance, currency, status "
          + "FROM accounts "
          + "WHERE id = ? LIMIT 1";

  private static final String SQL_SELECT_BY_USER_ID =
      "SELECT id, user_id, account_number, balance, currency, status "
          + "FROM accounts "
          + "WHERE user_id = ? LIMIT 1";

  private static final String USER_ID_UNIQUE_KEY = "uk_accounts_user_id";
  // SQLState de PostgreSQL para violacion de restriccion UNIQUE
  private static final String UNIQUE_VIOLATION = "23505";

  private final DataSource dataSource;

  @Override
  public AccountModel save(final AccountModel account) {
    final AccountPersistenceDto dto = AccountPersistenceMapper.fromModelToDto(account);
    executeSave(dto);
    return getById(account.getId())
        .orElseThrow(() -> AccountNotFoundException.becauseIdWasNotFound(dto.id()));
  }

  @Override
  public Optional<AccountModel> getById(final AccountId accountId) {
    try (final Connection connection = dataSource.getConnection();
        final PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {
      statement.setString(1, accountId.value());
      return readSingle(statement);
    } catch (final SQLException exception) {
      throw PersistenceException.becauseFindByIdFailed(accountId.value(), exception);
    }
  }

  @Override
  public Optional<AccountModel> getByUserId(final UserId userId) {
    try (final Connection connection = dataSource.getConnection();
        final PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_USER_ID)) {
      statement.setString(1, userId.value());
      return readSingle(statement);
    } catch (final SQLException exception) {
      throw PersistenceException.becauseFindByUserIdFailed(userId.value(), exception);
    }
  }

  private void executeSave(final AccountPersistenceDto dto) {
    try (final Connection connection = dataSource.getConnection();
        final PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
      statement.setString(1, dto.id());
      statement.setString(2, dto.userId());
      statement.setString(3, dto.accountNumber());
      statement.setBigDecimal(4, dto.balance());
      statement.setString(5, dto.currency());
      statement.setString(6, dto.status());
      statement.executeUpdate();
    } catch (final SQLException exception) {
      // Dos peticiones simultaneas para el mismo usuario: la BD es la ultima defensa
      if (isUserIdDuplicate(exception)) {
        throw AccountAlreadyExistsException.becauseUserAlreadyHasAccount(dto.userId());
      }
      throw PersistenceException.becauseSaveFailed(dto.id(), exception);
    }
  }

  private static Optional<AccountModel> readSingle(final PreparedStatement statement)
      throws SQLException {
    try (final ResultSet resultSet = statement.executeQuery()) {
      if (!resultSet.next()) {
        return Optional.empty();
      }
      return Optional.of(AccountPersistenceMapper.fromResultSetToModel(resultSet));
    }
  }

  private static boolean isUserIdDuplicate(final SQLException exception) {
    return UNIQUE_VIOLATION.equals(exception.getSQLState())
        && Objects.nonNull(exception.getMessage())
        && exception.getMessage().contains(USER_ID_UNIQUE_KEY);
  }
}
