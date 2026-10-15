package com.jcaa.accounts.infrastructure.adapter.persistence.config;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

/** Valida el nombre de la tabla de Users antes de usarlo en SQL (no admite parametros JDBC). */
@UtilityClass
public class UsersTableName {

  private static final Pattern FORMAT = Pattern.compile("[A-Za-z_][A-Za-z0-9_.]*");

  public String requireValid(final String tableName) {
    if (tableName == null || !FORMAT.matcher(tableName).matches()) {
      throw new IllegalArgumentException("Invalid users table name: " + tableName);
    }
    return tableName;
  }
}
