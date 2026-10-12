package com.jcaa.usersmanagement.infrastructure.adapter.email;

import com.jcaa.usersmanagement.application.port.out.EmailSenderPort;
import com.jcaa.usersmanagement.domain.exception.EmailSenderException;
import com.jcaa.usersmanagement.domain.model.EmailDestinationModel;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Envia correo transaccional por la API REST de Brevo (POST /smtp/email). */
@Slf4j
@Component
public class BrevoEmailSenderAdapter implements EmailSenderPort {

  private static final String HEADER_API_KEY = "api-key";
  private static final String LOG_SENT = "[BrevoEmailSenderAdapter] correo aceptado por Brevo.";

  private final RestClient restClient;
  private final BrevoProperties properties;

  public BrevoEmailSenderAdapter(
      final RestClient brevoRestClient, final BrevoProperties properties) {
    this.restClient = brevoRestClient;
    this.properties = properties;
  }

  @Override
  public void send(final EmailDestinationModel destination) {
    try {
      restClient
          .post()
          .header(HEADER_API_KEY, properties.apiKey())
          .body(buildPayload(destination))
          .retrieve()
          .toBodilessEntity();
      log.info(LOG_SENT);
    } catch (final RestClientException exception) {
      throw EmailSenderException.becauseDeliveryFailed(exception.getMessage());
    }
  }

  private Map<String, Object> buildPayload(final EmailDestinationModel destination) {
    return Map.of(
        "sender", Map.of("name", properties.senderName(), "email", properties.senderEmail()),
        "to",
            List.of(
                Map.of(
                    "email", destination.getDestinationEmail(),
                    "name", destination.getDestinationName())),
        "subject", destination.getSubject(),
        "htmlContent", destination.getBody());
  }
}
