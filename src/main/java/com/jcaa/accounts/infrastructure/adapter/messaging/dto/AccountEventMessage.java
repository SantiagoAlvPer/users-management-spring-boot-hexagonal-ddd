package com.jcaa.accounts.infrastructure.adapter.messaging.dto;

import java.util.Map;

/** Contrato JSON publicado en Kafka (mismo formato que user.created). */
public record AccountEventMessage(String eventName, String occurredOn, Map<String, String> payload) {}
