package com.jcaa.notifiers.infrastructure.adapter.messaging.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEventKafkaMessage {

  private String eventName;
  private String eventType;
  private String occurredOn;
  private Map<String, Object> payload;

  // Campos planos (fallback)
  private String name;
  private String email;
  private String password;
  private String role;
  private String status;

  public String getEffectiveEventName() {
    if (eventName != null && !eventName.isBlank()) {
      return eventName;
    }
    return eventType != null ? eventType : "";
  }

  public String getEffectiveEmail() {
    if (payload != null && payload.get("email") != null) {
      return String.valueOf(payload.get("email"));
    }
    return email;
  }

  public String getEffectiveName() {
    if (payload != null && payload.get("name") != null) {
      return String.valueOf(payload.get("name"));
    }
    return name != null && !name.isBlank() ? name : "Usuario";
  }

  public String getEffectiveRole() {
    if (payload != null && payload.get("role") != null) {
      return String.valueOf(payload.get("role"));
    }
    return role != null ? role : "MEMBER";
  }

  public String getEffectiveStatus() {
    if (payload != null && payload.get("status") != null) {
      return String.valueOf(payload.get("status"));
    }
    return status != null ? status : "ACTIVE";
  }

  public String getEffectivePassword() {
    if (payload != null && payload.get("password") != null) {
      return String.valueOf(payload.get("password"));
    }
    return password;
  }
}
