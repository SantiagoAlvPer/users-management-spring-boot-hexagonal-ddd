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
public class AccountEventKafkaMessage {

  private String eventName;
  private String occurredOn;
  private Map<String, Object> payload;

  // Campos planos (fallback)
  private String id;
  private String userId;
  private String accountNumber;
  private String balance;
  private String currency;
  private String status;
  private String name;
  private String email;

  public String getEffectiveEmail() {
    if (payload != null) {
      for (final Map.Entry<String, Object> entry : payload.entrySet()) {
        if (entry.getKey() != null
            && entry.getKey().equalsIgnoreCase("email")
            && entry.getValue() != null) {
          return String.valueOf(entry.getValue());
        }
      }
      for (final Map.Entry<String, Object> entry : payload.entrySet()) {
        if (entry.getKey() != null
            && entry.getKey().toLowerCase().contains("email")
            && entry.getValue() != null) {
          return String.valueOf(entry.getValue());
        }
      }
    }
    return email;
  }

  public String getEffectiveName() {
    if (payload != null) {
      for (final Map.Entry<String, Object> entry : payload.entrySet()) {
        if (entry.getKey() != null
            && (entry.getKey().equalsIgnoreCase("name")
                || entry.getKey().equalsIgnoreCase("userName"))
            && entry.getValue() != null) {
          return String.valueOf(entry.getValue());
        }
      }
    }
    return name != null && !name.isBlank() ? name : "Titular";
  }

  public String getEffectiveAccountNumber() {
    if (payload != null) {
      for (final Map.Entry<String, Object> entry : payload.entrySet()) {
        if (entry.getKey() != null
            && entry.getKey().equalsIgnoreCase("accountNumber")
            && entry.getValue() != null) {
          return String.valueOf(entry.getValue());
        }
      }
    }
    return accountNumber != null ? accountNumber : "N/A";
  }

  public String getEffectiveBalance() {
    if (payload != null) {
      for (final Map.Entry<String, Object> entry : payload.entrySet()) {
        if (entry.getKey() != null
            && entry.getKey().equalsIgnoreCase("balance")
            && entry.getValue() != null) {
          return String.valueOf(entry.getValue());
        }
      }
    }
    return balance != null ? balance : "0.0";
  }

  public String getEffectiveCurrency() {
    if (payload != null) {
      for (final Map.Entry<String, Object> entry : payload.entrySet()) {
        if (entry.getKey() != null
            && entry.getKey().equalsIgnoreCase("currency")
            && entry.getValue() != null) {
          return String.valueOf(entry.getValue());
        }
      }
    }
    return currency != null ? currency : "COP";
  }

  public String getEffectiveStatus() {
    if (payload != null) {
      for (final Map.Entry<String, Object> entry : payload.entrySet()) {
        if (entry.getKey() != null
            && entry.getKey().equalsIgnoreCase("status")
            && entry.getValue() != null) {
          return String.valueOf(entry.getValue());
        }
      }
    }
    return status != null ? status : "ACTIVE";
  }
}
