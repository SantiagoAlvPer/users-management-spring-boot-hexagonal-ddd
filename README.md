# Microservicio Notifiers (Gestión de Notificaciones)

Microservicio desacoplado de notificaciones por correo electrónico, construido con **Java 17**, **Spring Boot 3.3.5**, **Arquitectura Hexagonal (Puertos y Adaptadores)** y **Domain-Driven Design (DDD)**.

Este componente forma parte de la arquitectura de microservicios basada en eventos (Event-Driven Architecture):
- **Paso 3:** Consume eventos asíncronos (`USER_CREATED`, `USER_UPDATED`) publicados en el tópico de **Aiven Kafka**.
- **Paso 4:** Despacha de forma síncrona y resiliente correos transaccionales a través de la **API REST de Brevo** (`POST /v3/smtp/email`).

---

## 🏛️ Arquitectura Hexagonal y DDD

La estructura del proyecto mantiene una estricta separación de responsabilidades:

```
src/main/java/com/jcaa/notifiers/
├── domain/                                          # Capa de Dominio (Reglas y modelos de negocio)
│   ├── model/
│   │   └── EmailDestinationModel.java               # Entidad inmutable con validaciones de destino
│   └── exception/
│       ├── DomainException.java                     # Excepción base del dominio
│       ├── EmailSenderException.java                # Excepción ante fallos de envío
│       └── NotificationProcessingException.java     # Excepción en el procesamiento del evento
│
├── application/                                     # Capa de Aplicación (Casos de uso y orquestación)
│   ├── port/
│   │   ├── in/
│   │   │   └── SendNotificationUseCase.java         # Puerto de entrada (casos de uso de notificación)
│   │   └── out/
│   │       └── EmailSenderPort.java                 # Puerto de salida para el proveedor de correo
│   └── service/
│       ├── EmailNotificationService.java            # Renderiza plantillas HTML y delega al puerto
│       └── dto/
│           ├── UserCreatedNotificationCommand.java  # Comando de creación de usuario
│           └── UserUpdatedNotificationCommand.java  # Comando de actualización de usuario
│
├── infrastructure/                                  # Capa de Infraestructura (Adaptadores y Configuración)
│   ├── adapter/
│   │   ├── email/                                   # Adaptador de salida (Brevo REST + Resiliencia)
│   │   │   ├── BrevoConfig.java
│   │   │   ├── BrevoEmailRequest.java
│   │   │   └── BrevoEmailSenderAdapter.java         # RestClient + Retry + Circuit Breaker + Idempotencia
│   │   └── messaging/kafka/                         # Adaptador de entrada (Consumidor Aiven Kafka)
│   │       ├── dto/UserEventKafkaMessage.java
│   │       └── UserEventsKafkaConsumer.java         # Listener de eventos de Kafka
│   ├── entrypoint/rest/                             # Adaptador de entrada REST secundario
│   │   ├── controller/NotificationHealthController.java # Health check y endpoints de prueba
│   │   └── advice/GlobalExceptionHandler.java
│   └── config/
│       ├── BrevoSpringConfig.java                   # Configuración del bean RestClient y credenciales
│       └── KafkaConsumerConfig.java                 # Configuración de consumidor Kafka (PLAINTEXT / SSL / SASL_SSL)
│
└── NotifiersApplication.java                        # Composición raíz de Spring Boot
```

---

## ⚡ Formato del Mensaje en Kafka

El consumidor escucha en el tópico configurado en `kafka.topic.user-notifications` (por defecto `user-notifications`).

### 1. Usuario Creado (`USER_CREATED`)
```json
{
  "eventType": "USER_CREATED",
  "name": "Sebastian Vitola",
  "email": "sebastian@example.com",
  "password": "PasswordTemporal123!",
  "role": "ADMIN"
}
```

### 2. Usuario Actualizado (`USER_UPDATED`)
```json
{
  "eventType": "USER_UPDATED",
  "name": "Sebastian Vitola",
  "email": "sebastian@example.com",
  "role": "MEMBER",
  "status": "ACTIVE"
}
```

---

## 🛡️ Resiliencia en Brevo (Resilience4j)

El adaptador `BrevoEmailSenderAdapter` implementa:
1. **Reintentos con Backoff Exponencial:** Reintenta automáticamente ante errores transitorios de red, `408 Request Timeout`, `429 Too Many Requests` y `5xx Server Error`.
2. **Circuit Breaker:** Protege el sistema abriendo el circuito si la tasa de fallos supera el umbral configurado, evitando sobrecargar al proveedor o degradar recursos.
3. **Idempotencia:** Incluye una clave UUID (`idempotencyKey`) en la cabecera para evitar correos duplicados ante reintentos.
4. **Timeouts:** Timeouts configurables de conexión y lectura para evitar hilos bloqueados.

---

## ⚙️ Variables de Entorno

| Variable | Descripción | Valor por Defecto |
|---|---|---|
| `PORT` | Puerto HTTP del microservicio | `8080` |
| `KAFKA_BOOTSTRAP_SERVERS` | Servidores Kafka (ej. Aiven `kafka-xxx.aivencloud.com:12345`) | `localhost:9092` |
| `KAFKA_CONSUMER_GROUP_ID` | Group ID del consumidor | `notifiers-group` |
| `KAFKA_TOPIC_USER_NOTIFICATIONS` | Nombre del tópico | `user-notifications` |
| `KAFKA_SECURITY_PROTOCOL` | Protocolo de seguridad (`PLAINTEXT`, `SSL`, `SASL_SSL`) | `PLAINTEXT` |
| `KAFKA_SASL_MECHANISM` | Mecanismo SASL (ej. `SCRAM-SHA-256`) | *(opcional)* |
| `KAFKA_SASL_JAAS_CONFIG` | Configuración JAAS con usuario y contraseña | *(opcional)* |
| `BREVO_API_KEY` | Clave API REST de Brevo (prefijo `xkeysib-`) | *(requerido en prod)* |
| `BREVO_FROM_ADDRESS` | Correo verificado del remitente en Brevo | *(requerido en prod)* |
| `BREVO_FROM_NAME` | Nombre del remitente que aparece en el correo | `Notificaciones` |

---

## 🚀 Despliegue en Render

El archivo `render.yaml` define el servicio web para Render usando Docker. Al crear el Blueprint en Render:
1. Conectar este repositorio.
2. Configurar las variables secretas requeridas: `BREVO_API_KEY`, `BREVO_FROM_ADDRESS`, y las credenciales de Aiven Kafka (`KAFKA_BOOTSTRAP_SERVERS`, etc.).
3. Desplegar. El servicio responderá al health check en `/health`.
