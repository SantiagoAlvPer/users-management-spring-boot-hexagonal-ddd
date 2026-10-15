# Accounts Service — Spring Boot, Hexagonal + DDD

Microservicio de cuentas (wallet). Recibe la orden de crear la cuenta desde **Users**, lee el correo del usuario de la tabla `users` (misma base PostgreSQL que Users), guarda la cuenta en **PostgreSQL** y publica el evento `account.created` (con el correo) en **Kafka (Aiven)**.

```
Paso 5: Users  --POST /api/accounts (sync)-->  accounts
        accounts --SELECT email FROM users (sync)--> PostgreSQL (tabla de Users)
Paso 6: accounts --INSERT accounts (sync)-->   PostgreSQL
Paso 7: accounts --account.created (async)-->  Kafka (Aiven)
```

## Arquitectura

```
domain/                    Java puro: AccountModel, value objects, enums, eventos, excepciones
application/
  port/in/                 CreateAccountUseCase, GetAccountByIdUseCase, GetAccountByUserIdUseCase,
                           DeleteAccountUseCase
  port/out/                SaveAccountPort, GetAccountByIdPort, GetAccountByUserIdPort, DeleteAccountPort,
                           GetUserEmailPort, PublishAccountCreatedEventPort
  service/                 Implementaciones de los casos de uso (+ dto command/query, mapper)
infrastructure/
  adapter/persistence/     AccountRepositoryPostgres (JDBC puro, sin JPA)
                           UserEmailRepositoryPostgres (solo lectura de la tabla users)
  adapter/messaging/       KafkaAccountEventPublisherAdapter (productor Kafka)
  entrypoint/rest/         AccountRestController + GlobalExceptionHandler
  config/                  DataSourceSpringConfig (HikariCP)
```

Regla de dependencias: `domain <- application <- infrastructure`.

## API

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| `POST` | `/api/accounts` | Crea la cuenta de un usuario (lo llama Users) | 201, 400, 404, 409 |
| `GET` | `/api/accounts/{id}` | Cuenta por id | 200, 404 |
| `GET` | `/api/accounts/user/{userId}` | Cuenta de un usuario | 200, 404 |
| `DELETE` | `/api/accounts/{id}` | Borra una cuenta | 204, 404 |
| `GET` | `/actuator/health` | Health check (Render) | 200 |

Body de creación (`currency` es opcional, `COP` por defecto; soporta `COP` y `USD`):

```json
{ "userId": "8f684dc4-0c67-4813-8d58-7b5e7f7937b8", "currency": "COP" }
```

Respuesta `201`:

```json
{
  "id": "2b0b8a59-6a43-4d0e-9a59-1f0f2d1c4c11",
  "userId": "8f684dc4-0c67-4813-8d58-7b5e7f7937b8",
  "accountNumber": "4821937750",
  "balance": 0.00,
  "currency": "COP",
  "status": "ACTIVE"
}
```

Reglas: una cuenta por usuario (`409` si ya existe), saldo inicial `0.00`, estado `ACTIVE`, número de cuenta de 10 dígitos generado por el servicio. Antes de guardar se lee el correo del usuario en la tabla `users`: si el usuario no existe responde `404` y no guarda nada.

## Contrato del evento (topic `account.created`)

Key = `id` de la cuenta. Valor JSON (String), mismo formato que `user.created`:

```json
{
  "eventName": "account.created",
  "occurredOn": "2026-01-01T10:00:00",
  "payload": {
    "id": "2b0b8a59-...", "userId": "8f684dc4-...", "email": "john@example.com",
    "accountNumber": "4821937750",
    "balance": "0.00", "currency": "COP", "status": "ACTIVE"
  }
}
```

La publicación es asíncrona: si Kafka falla, el error queda en el log y la cuenta sigue creada (la petición HTTP no se cae).

## Configuración (variables de entorno)

| Variable | Descripción | Default |
|---|---|---|
| `DB_HOST` | Host de PostgreSQL | `localhost` |
| `DB_PORT` | Puerto | `5432` |
| `DB_NAME` | Base de datos | `wallet` |
| `DB_USERNAME` | Usuario | `postgres` |
| `DB_PASSWORD` | Contraseña | `postgres` |
| `DB_SSL_MODE` | `prefer` con el host interno de Render, `require` con el externo | `prefer` |
| `USERS_TABLE_NAME` | Tabla de usuarios de Users (columnas `id` y `email`) | `users` |
| `KAFKA_BOOTSTRAP_SERVERS` | Brokers | `localhost:9092` |
| `KAFKA_TOPIC_ACCOUNT_CREATED` | Topic | `account.created` |
| `SPRING_PROFILES_ACTIVE` | `aiven` para conectarse a Aiven | — |
| `KAFKA_USERNAME` / `KAFKA_PASSWORD` | Usuario SASL de Aiven (perfil `aiven`) | — |
| `KAFKA_CA_CERT` | Contenido completo de `ca.pem` de Aiven (perfil `aiven`) | — |

### Base compartida con Users
Accounts usa la **misma base PostgreSQL que Users**: las variables `DB_*` deben ser las mismas que tiene Users. Accounts crea y escribe solo su tabla `accounts`; de la tabla `users` únicamente lee `email` por `id` (sirve si `id` es `VARCHAR` o `UUID`).

La tabla `accounts` se crea sola al arrancar (`schema.sql`, `CREATE TABLE IF NOT EXISTS`).

### PostgreSQL en Render
1. En Render: **New → Postgres**, plan **Free**, misma región que el web service. Si el equipo ya tiene una base Postgres (por ejemplo la de Users), se puede reutilizar: accounts solo usa su propia tabla `accounts`.
2. En la base → **Connections**: copiar los datos **Internal** (Hostname, Port, Database, Username, Password) a las variables `DB_*` del servicio accounts.
3. El host interno solo funciona entre servicios de Render. Para conectarse desde tu PC usa el host **External** con `DB_SSL_MODE=require`.

### Aiven Kafka
1. En el servicio Kafka: *Advanced configuration* → activar `kafka_authentication_methods.sasl`.
2. En *Connection information* → pestaña **SASL**: copiar *Service URI* (va en `KAFKA_BOOTSTRAP_SERVERS`), usuario, contraseña y descargar el **CA certificate**.
3. Crear el topic `account.created` en la pestaña *Topics* (Aiven no los crea automáticamente).

## Probar local

```bash
docker compose up -d          # PostgreSQL + Kafka
./mvnw spring-boot:run

# crear cuenta
curl -X POST localhost:8080/api/accounts -H "Content-Type: application/json" -d '{"userId":"u-1"}'

# ver el evento publicado
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 --topic account.created --from-beginning
```

Colección de Postman: `postman/accounts-service.postman_collection.json` (variable `baseUrl`).

## Despliegue en Render

`render.yaml` crea el web service con Docker en plan free. Llenar en Render las variables marcadas `sync: false`. El plan free se duerme tras inactividad: la primera llamada desde Users puede tardar ~1 min, así que conviene que Users use un timeout amplio para el Paso 5.

## Verificación

```bash
./mvnw clean verify     # tests + reporte de cobertura en target/site/jacoco
```
