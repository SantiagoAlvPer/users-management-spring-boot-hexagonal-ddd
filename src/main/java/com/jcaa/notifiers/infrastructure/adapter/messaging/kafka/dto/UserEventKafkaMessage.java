package com.jcaa.notifiers.infrastructure.adapter.messaging.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

  private String eventType;
  private String name;
  private String email;
  private String password;
  private String role;
  private String status;
}
