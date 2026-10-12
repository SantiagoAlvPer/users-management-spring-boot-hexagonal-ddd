package com.jcaa.usersmanagement.infrastructure.adapter.email;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.jcaa.usersmanagement.domain.exception.EmailSenderException;
import com.jcaa.usersmanagement.domain.model.EmailDestinationModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@DisplayName("BrevoEmailSenderAdapter")
class BrevoEmailSenderAdapterTest {

  private static final String URL = "https://brevo.test/v3/smtp/email";

  private MockRestServiceServer server;
  private BrevoEmailSenderAdapter adapter;
  private EmailDestinationModel destination;

  @BeforeEach
  void setUp() {
    final RestClient.Builder builder =
        RestClient.builder().baseUrl(URL).defaultHeader("accept", MediaType.APPLICATION_JSON_VALUE);
    server = MockRestServiceServer.bindTo(builder).build();
    final BrevoProperties properties =
        new BrevoProperties(URL, "key-123", "sender@test.local", "Sender", 1000, 1000);
    adapter = new BrevoEmailSenderAdapter(builder.build(), properties);
    destination = new EmailDestinationModel("to@test.local", "To", "Subject", "<p>Hi</p>");
  }

  @Test
  @DisplayName("send() hace POST con api-key y payload de Brevo")
  void shouldPostPayloadWithApiKey() {
    // Arrange
    server
        .expect(requestTo(URL))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("api-key", "key-123"))
        .andExpect(jsonPath("$.sender.email").value("sender@test.local"))
        .andExpect(jsonPath("$.to[0].email").value("to@test.local"))
        .andExpect(jsonPath("$.htmlContent").value("<p>Hi</p>"))
        .andRespond(withSuccess("{\"messageId\":\"x\"}", MediaType.APPLICATION_JSON));

    // Act
    adapter.send(destination);

    // Assert
    server.verify();
  }

  @Test
  @DisplayName("send() lanza EmailSenderException cuando Brevo responde 5xx")
  void shouldThrowWhenBrevoFails() {
    // Arrange
    server.expect(requestTo(URL)).andRespond(withServerError());

    // Act & Assert
    assertThrows(EmailSenderException.class, () -> adapter.send(destination));
  }
}
