package com.jcaa.accounts.infrastructure.adapter.persistence.mapper;

import com.jcaa.accounts.domain.enums.AccountCurrency;
import com.jcaa.accounts.domain.enums.AccountStatus;
import com.jcaa.accounts.domain.model.AccountModel;
import com.jcaa.accounts.domain.valueobject.AccountBalance;
import com.jcaa.accounts.domain.valueobject.AccountId;
import com.jcaa.accounts.domain.valueobject.AccountNumber;
import com.jcaa.accounts.domain.valueobject.UserEmail;
import com.jcaa.accounts.domain.valueobject.UserId;
import com.jcaa.accounts.infrastructure.adapter.persistence.dto.AccountPersistenceDto;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AccountPersistenceMapper {

  private static final String COL_ID = "id";
  private static final String COL_USER_ID = "user_id";
  private static final String COL_ACCOUNT_NUMBER = "account_number";
  private static final String COL_BALANCE = "balance";
  private static final String COL_CURRENCY = "currency";
  private static final String COL_STATUS = "status";
  private static final String COL_OWNER_EMAIL = "owner_email";

  public AccountPersistenceDto fromModelToDto(final AccountModel account) {
    return new AccountPersistenceDto(
        account.getId().value(),
        account.getUserId().value(),
        account.getAccountNumber().value(),
        account.getBalance().value(),
        account.getCurrency().name(),
        account.getStatus().name());
  }

  public AccountModel fromResultSetToModel(final ResultSet resultSet) throws SQLException {
    return new AccountModel(
        new AccountId(resultSet.getString(COL_ID)),
        new UserId(resultSet.getString(COL_USER_ID)),
        new AccountNumber(resultSet.getString(COL_ACCOUNT_NUMBER)),
        new AccountBalance(resultSet.getBigDecimal(COL_BALANCE)),
        AccountCurrency.fromString(resultSet.getString(COL_CURRENCY)),
        AccountStatus.fromString(resultSet.getString(COL_STATUS)),
        toOwnerEmail(resultSet.getString(COL_OWNER_EMAIL)));
  }

  private UserEmail toOwnerEmail(final String email) {
    return Objects.isNull(email) ? null : new UserEmail(email);
  }
}
