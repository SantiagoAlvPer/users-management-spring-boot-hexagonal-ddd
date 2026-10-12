package com.jcaa.usersmanagement.infrastructure.entrypoint.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserCreatedEventMessage(String eventName, Payload payload) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Payload(String id, String name, String email, String role, String status) {}
}
