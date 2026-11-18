package com.jcaa.usersmanagement.infrastructure.config;

import com.jcaa.usersmanagement.infrastructure.adapter.persistence.config.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Objects;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class DataSourceSpringConfig {

  private static final String PROP_DB_HOST     = "${db.host}";
  private static final String PROP_DB_PORT     = "${db.port}";
  private static final String PROP_DB_NAME     = "${db.name}";
  private static final String PROP_DB_USERNAME = "${db.username}";
  private static final String PROP_DB_PASSWORD = "${db.password}";
  private static final String PROP_DB_SSL_MODE = "${db.ssl-mode}";
  private static final String PROP_DB_URL      = "${db.url:}";

  private static final String LOG_DATASOURCE_INIT = "[DataSourceSpringConfig] DataSource inicializado. host={} port={}";
  private static final String LOG_DATASOURCE_INIT_URL = "[DataSourceSpringConfig] DataSource inicializado con URL directa.";

  @Value(PROP_DB_HOST)
  private String dbHost;

  @Value(PROP_DB_PORT)
  private int dbPort;

  @Value(PROP_DB_NAME)
  private String dbName;

  @Value(PROP_DB_USERNAME)
  private String dbUsername;

  @Value(PROP_DB_PASSWORD)
  private String dbPassword;

  @Value(PROP_DB_SSL_MODE)
  private String dbSslMode;

  @Value(PROP_DB_URL)
  private String dbUrl;

  @Bean
  public DataSource dataSource() {
    final HikariConfig hikariConfig = new HikariConfig();

    if (Objects.nonNull(dbUrl) && !dbUrl.isBlank()) {
      hikariConfig.setJdbcUrl(dbUrl);
      if (Objects.nonNull(dbUsername) && !dbUsername.isBlank()) {
        hikariConfig.setUsername(dbUsername);
      }
      if (Objects.nonNull(dbPassword) && !dbPassword.isBlank()) {
        hikariConfig.setPassword(dbPassword);
      }
      log.info(LOG_DATASOURCE_INIT_URL);
    } else {
      final DatabaseConfig config =
          new DatabaseConfig(dbHost, dbPort, dbName, dbUsername, dbPassword, dbSslMode);
      hikariConfig.setJdbcUrl(config.buildJdbcUrl());
      hikariConfig.setUsername(config.username());
      hikariConfig.setPassword(config.password());
      log.info(LOG_DATASOURCE_INIT, dbHost, dbPort);
    }

    hikariConfig.setMaximumPoolSize(10);
    hikariConfig.setMinimumIdle(2);
    hikariConfig.setConnectionTimeout(30_000);

    return new HikariDataSource(hikariConfig);
  }
}

