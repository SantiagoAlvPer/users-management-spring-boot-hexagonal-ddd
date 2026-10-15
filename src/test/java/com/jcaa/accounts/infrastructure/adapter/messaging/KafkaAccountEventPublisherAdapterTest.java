package com.jcaa.accounts.infrastructure.adapter.messaging;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.accounts.domain.enums.AccountCurrency;
import com.jcaa.accounts.domain.event.AccountCreatedDomainEvent;
import com.jcaa.accounts.domain.model.AccountModel;
import com.jcaa.accounts.domain.valueobject.AccountId;
import com.jcaa.accounts.domain.valueobject.AccountNumber;
import com.jcaa.accounts.domain.valueobject.UserEmail;
import com.jcaa.accounts.domain.valueobject.UserId;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@DisplayName("KafkaAccountEventPublisherAdapter")
@ExtendWith(MockitoExtension.class)
class KafkaAccountEventPublisherAdapterTest {

  private static final String TOPIC = "account.created";

  @Mock private KafkaTemplate<String, String> kafkaTemplate;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private KafkaAccountEventPublisherAdapter adapter;
  private AccountCreatedDomainEvent event;

  @BeforeEach
  void setUp() {
    adapter = new KafkaAccountEventPublisherAdapter(kafkaTemplate, objectMapper, TOPIC);
    event =
        new AccountCreatedDomainEvent(
            AccountModel.open(
                new AccountId("a-1"),
                new UserId("u-1"),
                new AccountNumber("1234567890"),
                AccountCurrency.COP),
            new UserEmail("john@example.com"));
  }

  @Test
  @DisplayName("publish() envia el JSON al topic con key = accountId")
  void shouldSendJsonWithAccountIdAsKey() throws Exception {
    // Arrange
    final CompletableFuture<SendResult<String, String>> pending = new CompletableFuture<>();
    when(kafkaTemplate.send(eq(TOPIC), eq("a-1"), anyString())).thenReturn(pending);

    // Act
    adapter.publish(event);

    // Assert
    final ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
    verify(kafkaTemplate).send(eq(TOPIC), eq("a-1"), jsonCaptor.capture());
    final JsonNode json = objectMapper.readTree(jsonCaptor.getValue());
    assertAll(
        () -> assertEquals("account.created", json.get("eventName").asText()),
        () -> assertEquals("u-1", json.get("payload").get("userId").asText()),
        () -> assertEquals("john@example.com", json.get("payload").get("email").asText()),
        () -> assertEquals("1234567890", json.get("payload").get("accountNumber").asText()),
        () -> assertEquals("0.00", json.get("payload").get("balance").asText()));
  }

  @Test
  @DisplayName("publish() no propaga errores de Kafka (la cuenta ya quedo creada)")
  void shouldNotPropagateKafkaErrors() {
    // Arrange
    when(kafkaTemplate.send(eq(TOPIC), eq("a-1"), anyString()))
        .thenThrow(new KafkaException("broker down"));

    // Act & Assert
    assertDoesNotThrow(() -> adapter.publish(event));
  }

  @Test
  @DisplayName("publish() no propaga un fallo asincrono del envio")
  void shouldHandleAsyncFailure() {
    // Arrange
    when(kafkaTemplate.send(eq(TOPIC), eq("a-1"), anyString()))
        .thenReturn(CompletableFuture.failedFuture(new KafkaException("timeout")));

    // Act & Assert
    assertDoesNotThrow(() -> adapter.publish(event));
  }
}
