package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import com.jcaa.usersmanagement.application.port.in.ReindexUsersUseCase;
import com.jcaa.usersmanagement.application.port.in.SearchUsersUseCase;
import com.jcaa.usersmanagement.application.service.dto.query.SearchUsersQuery;
import com.jcaa.usersmanagement.application.service.dto.query.UserSearchResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search/users")
@RequiredArgsConstructor
@Tag(name = "Búsqueda de Usuarios", description = "Búsqueda estructurada con OpenSearch (CQRS)")
public class UserSearchRestController {

  private final SearchUsersUseCase searchUsersUseCase;
  private final ReindexUsersUseCase reindexUsersUseCase;

  @GetMapping
  @Operation(summary = "Buscar usuarios en OpenSearch por texto difuso y estado")
  public List<UserSearchResult> search(
      @RequestParam(value = "q", required = false) final String query,
      @RequestParam(value = "status", required = false) final String status) {
    return searchUsersUseCase.execute(new SearchUsersQuery(query, status));
  }

  @PostMapping("/reindex")
  @Operation(summary = "Reindexar todos los usuarios desde PostgreSQL hacia OpenSearch")
  public Map<String, Integer> reindex() {
    final int count = reindexUsersUseCase.execute();
    return Map.of("indexed", count);
  }
}
