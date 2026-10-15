package com.jcaa.accounts.infrastructure.entrypoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jcaa.accounts.application.port.in.CreateAccountUseCase;
import com.jcaa.accounts.application.port.in.GetAccountByIdUseCase;
import com.jcaa.accounts.application.port.in.GetAccountByUserIdUseCase;
import com.jcaa.accounts.application.service.dto.query.GetAccountByIdQuery;
import com.jcaa.accounts.application.service.dto.query.GetAccountByUserIdQuery;
import com.jcaa.accounts.domain.enums.AccountCurrency;
import com.jcaa.accounts.domain.exception.AccountAlreadyExistsException;
import com.jcaa.accounts.domain.exception.AccountNotFoundException;
import com.jcaa.accounts.domain.exception.UserNotFoundException;
import com.jcaa.accounts.domain.model.AccountModel;
import com.jcaa.accounts.domain.valueobject.AccountId;
import com.jcaa.accounts.domain.valueobject.AccountNumber;
import com.jcaa.accounts.domain.valueobject.UserId;
import com.jcaa.accounts.infrastructure.adapter.rest.users.exception.UsersServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AccountRestController.class)
@DisplayName("AccountRestController")
class AccountRestControllerTest {

  private static final AccountModel ACCOUNT =
      AccountModel.open(
          new AccountId("a-1"),
          new UserId("u-1"),
          new AccountNumber("1234567890"),
          AccountCurrency.COP);

  @Autowired private MockMvc mockMvc;

  @MockBean private CreateAccountUseCase createAccountUseCase;
  @MockBean private GetAccountByIdUseCase getAccountByIdUseCase;
  @MockBean private GetAccountByUserIdUseCase getAccountByUserIdUseCase;

  @Test
  @DisplayName("POST /api/accounts devuelve 201 con la cuenta creada")
  void shouldCreateAccount() throws Exception {
    // Arrange
    when(createAccountUseCase.execute(any())).thenReturn(ACCOUNT);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u-1\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("a-1"))
        .andExpect(jsonPath("$.userId").value("u-1"))
        .andExpect(jsonPath("$.accountNumber").value("1234567890"))
        .andExpect(jsonPath("$.balance").value(0.0))
        .andExpect(jsonPath("$.currency").value("COP"))
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  @DisplayName("POST /api/accounts devuelve 400 si falta userId")
  void shouldRejectMissingUserId() throws Exception {
    // Act & Assert
    mockMvc
        .perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
    verifyNoInteractions(createAccountUseCase);
  }

  @Test
  @DisplayName("POST /api/accounts devuelve 409 si el usuario ya tiene cuenta")
  void shouldReturnConflictWhenAccountExists() throws Exception {
    // Arrange
    when(createAccountUseCase.execute(any()))
        .thenThrow(AccountAlreadyExistsException.becauseUserAlreadyHasAccount("u-1"));

    // Act & Assert
    mockMvc
        .perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u-1\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("POST /api/accounts devuelve 404 si el usuario no existe en Users")
  void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
    // Arrange
    when(createAccountUseCase.execute(any()))
        .thenThrow(UserNotFoundException.becauseIdWasNotFound("u-9"));

    // Act & Assert
    mockMvc
        .perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u-9\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/accounts devuelve 502 si el servicio de Users falla")
  void shouldReturnBadGatewayWhenUsersServiceFails() throws Exception {
    // Arrange
    when(createAccountUseCase.execute(any()))
        .thenThrow(UsersServiceException.becauseRequestFailed("u-1", new RuntimeException("down")));

    // Act & Assert
    mockMvc
        .perform(
            post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u-1\"}"))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.status").value(502));
  }

  @Test
  @DisplayName("GET /api/accounts/{id} devuelve la cuenta")
  void shouldGetById() throws Exception {
    // Arrange
    when(getAccountByIdUseCase.execute(new GetAccountByIdQuery("a-1"))).thenReturn(ACCOUNT);

    // Act & Assert
    mockMvc
        .perform(get("/api/accounts/a-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("a-1"));
  }

  @Test
  @DisplayName("GET /api/accounts/{id} devuelve 404 si no existe")
  void shouldReturnNotFound() throws Exception {
    // Arrange
    when(getAccountByIdUseCase.execute(new GetAccountByIdQuery("x")))
        .thenThrow(AccountNotFoundException.becauseIdWasNotFound("x"));

    // Act & Assert
    mockMvc.perform(get("/api/accounts/x")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/accounts/user/{userId} devuelve la cuenta del usuario")
  void shouldGetByUserId() throws Exception {
    // Arrange
    when(getAccountByUserIdUseCase.execute(new GetAccountByUserIdQuery("u-1")))
        .thenReturn(ACCOUNT);

    // Act & Assert
    mockMvc
        .perform(get("/api/accounts/user/u-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value("u-1"));
  }
}
