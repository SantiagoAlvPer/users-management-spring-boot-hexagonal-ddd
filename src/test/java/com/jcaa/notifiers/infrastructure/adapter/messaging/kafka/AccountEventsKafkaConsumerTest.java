package com.jcaa.notifiers.infrastructure.adapter.messaging.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.notifiers.application.port.in.SendNotificationUseCase;
import com.jcaa.notifiers.application.service.dto.AccountCreatedNotificationCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("AccountEventsKafkaConsumer")
class AccountEventsKafkaConsumerTest {

  private SendNotificationUseCase sendNotificationUseCase;
  private ObjectMapper objectMapper;
  private AccountEventsKafkaConsumer consumer;

  @BeforeEach
  void setUp() {
    sendNotificationUseCase = mock(SendNotificationUseCase.class);
    objectMapper = new ObjectMapper();
    consumer = new AccountEventsKafkaConsumer(sendNotificationUseCase, objectMapper);
  }

  @Test
  @DisplayName("procesa evento account.created con campo Email en mayúscula en el payload")
  void shouldProcessAccountCreatedWithCapitalEmail() {
    final String json = """
        {
          "eventName": "account.created",
          "occurredOn": "2026-10-14T21:00:00",
          "payload": {
            "id": "acc-123",
            "userId": "usr-456",
            "accountNumber": "ACC-998877",
            "balance": "0.0",
            "currency": "COP",
            "status": "ACTIVE",
            "Email": "juanitoperez@gmail.com"
          }
        }
        """;

    consumer.consume(json);

    final ArgumentCaptor<AccountCreatedNotificationCommand> captor =
        ArgumentCaptor.forClass(AccountCreatedNotificationCommand.class);
    verify(sendNotificationUseCase).notifyAccountCreated(captor.capture());

    final AccountCreatedNotificationCommand command = captor.getValue();
    assertEquals("juanitoperez@gmail.com", command.email());
    assertEquals("ACC-998877", command.accountNumber());
    assertEquals("0.0", command.balance());
    assertEquals("COP", command.currency());
    assertEquals("ACTIVE", command.status());
    assertEquals("Titular", command.name());
  }

  @Test
  @DisplayName("procesa evento account.created con name y email en minúscula en el payload")
  void shouldProcessAccountCreatedWithLowercaseEmailAndName() {
    final String json = """
        {
          "eventName": "account.created",
          "occurredOn": "2026-10-14T21:00:00",
          "payload": {
            "id": "acc-999",
            "userId": "usr-888",
            "name": "Juan Perez",
            "accountNumber": "11223344",
            "balance": "50000.00",
            "currency": "USD",
            "status": "ACTIVE",
            "email": "juan@test.com"
          }
        }
        """;

    consumer.consume(json);

    final ArgumentCaptor<AccountCreatedNotificationCommand> captor =
        ArgumentCaptor.forClass(AccountCreatedNotificationCommand.class);
    verify(sendNotificationUseCase).notifyAccountCreated(captor.capture());

    final AccountCreatedNotificationCommand command = captor.getValue();
    assertEquals("juan@test.com", command.email());
    assertEquals("Juan Perez", command.name());
    assertEquals("11223344", command.accountNumber());
    assertEquals("50000.00", command.balance());
    assertEquals("USD", command.currency());
  }

  @Test
  @DisplayName("descarta mensaje sin direccion de correo sin lanzar excepcion")
  void shouldIgnoreMessageWithoutEmail() {
    final String json = """
        {
          "eventName": "account.created",
          "payload": {
            "id": "acc-123",
            "userId": "usr-456",
            "accountNumber": "ACC-998877"
          }
        }
        """;

    consumer.consume(json);

    verify(sendNotificationUseCase, never()).notifyAccountCreated(any());
  }

  @Test
  @DisplayName("maneja JSON malformado sin fallar")
  void shouldHandleMalformedJsonGracefully() {
    final String malformed = "{ broken json !!";

    consumer.consume(malformed);

    verify(sendNotificationUseCase, never()).notifyAccountCreated(any());
  }
}
