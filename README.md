# Notifications Service — Spring Boot, Hexagonal + DDD

Microservicio de notificaciones. Consume eventos de Kafka y envía correos con la API REST de Brevo.

```
usuario se registra -> evento `user.created` en Kafka -> notifications-service -> Brevo -> correo (Gmail)
```

El servicio de usuarios (productor) no vive en esta rama. Solo publica el evento.

## Contrato del evento (topic `user.created`)

Valor JSON (String):

```json
{
  "eventName": "user.created",
  "occurredOn": "2026-01-01T10:00:00",
  "payload": { "id": "u-1", "name": "John", "email": "john@example.com", "role": "ADMIN", "status": "ACTIVE" }
}
```

No se envía contraseña en el evento ni en el correo.

## Resiliencia

- Fallo de Brevo (5xx, timeout): reintentos con backoff exponencial (`notifications.retry.*`).
- Reintentos agotados o evento inválido (JSON roto, email inválido): el evento va a `user.created.DLT`.

## Configuración (variables de entorno)

| Variable | Descripción | Default |
|---|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | Brokers | `localhost:9092` |
| `KAFKA_GROUP_ID` | Consumer group | `notifications-service` |
| `KAFKA_TOPIC_USER_CREATED` | Topic | `user.created` |
| `BREVO_API_KEY` | API key de Brevo | — |
| `BREVO_SENDER_EMAIL` | Remitente verificado en Brevo (tu Gmail) | — |
| `BREVO_SENDER_NAME` | Nombre del remitente | `Gestion de Usuarios` |

En Brevo: crear API key y verificar el Gmail como sender (Senders, Domains & Dedicated IPs).

## Probar local

```bash
docker compose up -d
BREVO_API_KEY=... BREVO_SENDER_EMAIL=tu@gmail.com ./mvnw spring-boot:run
# publicar un evento
docker compose exec -T kafka /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 --topic user.created <<< \
  '{"eventName":"user.created","payload":{"id":"u-1","name":"John","email":"destino@example.com","role":"ADMIN","status":"ACTIVE"}}'
```

## Verificación

```bash
./mvnw clean test
```
