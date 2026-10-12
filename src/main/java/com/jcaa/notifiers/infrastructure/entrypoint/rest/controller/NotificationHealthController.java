package com.jcaa.notifiers.infrastructure.entrypoint.rest.controller;

import com.jcaa.notifiers.application.port.in.SendNotificationUseCase;
import com.jcaa.notifiers.application.service.dto.UserCreatedNotificationCommand;
import com.jcaa.notifiers.application.service.dto.UserUpdatedNotificationCommand;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationHealthController {

  private final SendNotificationUseCase sendNotificationUseCase;

  @GetMapping({"/", "/health"})
  public ResponseEntity<Map<String, Object>> health() {
    return ResponseEntity.ok(
        Map.of(
            "status", "UP",
            "service", "notifiers",
            "message", "Microservicio de Notificaciones activo y escuchando eventos"));
  }

  @PostMapping("/api/notifications/test/user-created")
  public ResponseEntity<Map<String, String>> testUserCreated(
      @Valid @RequestBody final UserCreatedNotificationCommand command) {
    log.info("[NotificationHealthController] Prueba manual de notificación de creación: {}", command.email());
    sendNotificationUseCase.notifyUserCreated(command);
    return ResponseEntity.ok(
        Map.of("message", "Notificación de usuario creado enviada exitosamente a " + command.email()));
  }

  @PostMapping("/api/notifications/test/user-updated")
  public ResponseEntity<Map<String, String>> testUserUpdated(
      @Valid @RequestBody final UserUpdatedNotificationCommand command) {
    log.info("[NotificationHealthController] Prueba manual de notificación de actualización: {}", command.email());
    sendNotificationUseCase.notifyUserUpdated(command);
    return ResponseEntity.ok(
        Map.of("message", "Notificación de usuario actualizado enviada exitosamente a " + command.email()));
  }
}
