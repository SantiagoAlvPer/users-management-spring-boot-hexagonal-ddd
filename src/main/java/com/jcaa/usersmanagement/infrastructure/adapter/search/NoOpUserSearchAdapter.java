package com.jcaa.usersmanagement.infrastructure.adapter.search;

import com.jcaa.usersmanagement.application.port.out.UserSearchIndexPort;
import com.jcaa.usersmanagement.application.service.dto.query.UserSearchResult;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "opensearch.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpUserSearchAdapter implements UserSearchIndexPort {

  @Override
  public void indexUser(final UserSearchResult document) {
    log.debug("[NoOpUserSearchAdapter] OpenSearch deshabilitado; se omite indexado.");
  }

  @Override
  public void deleteUser(final String userId) {
    log.debug("[NoOpUserSearchAdapter] OpenSearch deshabilitado; se omite borrado.");
  }

  @Override
  public List<UserSearchResult> search(final String query, final String status) {
    log.debug("[NoOpUserSearchAdapter] OpenSearch deshabilitado; retornando lista vacía.");
    return List.of();
  }
}
