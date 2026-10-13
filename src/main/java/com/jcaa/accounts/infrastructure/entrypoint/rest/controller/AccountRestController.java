package com.jcaa.accounts.infrastructure.entrypoint.rest.controller;

import com.jcaa.accounts.application.port.in.CreateAccountUseCase;
import com.jcaa.accounts.application.port.in.GetAccountByIdUseCase;
import com.jcaa.accounts.application.port.in.GetAccountByUserIdUseCase;
import com.jcaa.accounts.domain.model.AccountModel;
import com.jcaa.accounts.infrastructure.entrypoint.rest.dto.request.CreateAccountRestRequest;
import com.jcaa.accounts.infrastructure.entrypoint.rest.dto.response.AccountRestResponse;
import com.jcaa.accounts.infrastructure.entrypoint.rest.mapper.AccountRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountRestController {

  private final CreateAccountUseCase createAccountUseCase;
  private final GetAccountByIdUseCase getAccountByIdUseCase;
  private final GetAccountByUserIdUseCase getAccountByUserIdUseCase;

  /** Paso 5: el servicio de Users llama este endpoint de forma sincrona. */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AccountRestResponse create(@Valid @RequestBody final CreateAccountRestRequest request) {
    final AccountModel account =
        createAccountUseCase.execute(AccountRestMapper.toCreateCommand(request));
    return AccountRestMapper.toResponse(account);
  }

  @GetMapping("/{id}")
  public AccountRestResponse getById(@PathVariable final String id) {
    final AccountModel account =
        getAccountByIdUseCase.execute(AccountRestMapper.toGetByIdQuery(id));
    return AccountRestMapper.toResponse(account);
  }

  @GetMapping("/user/{userId}")
  public AccountRestResponse getByUserId(@PathVariable final String userId) {
    final AccountModel account =
        getAccountByUserIdUseCase.execute(AccountRestMapper.toGetByUserIdQuery(userId));
    return AccountRestMapper.toResponse(account);
  }
}
