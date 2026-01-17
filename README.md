# Couple Check-in Backend

Production-ready MVP backend for the “Couple Check-in” application.

## Stack
- Java 21 + Spring Boot 3
- PostgreSQL + Flyway
- Spring Data JPA
- JWT auth (email/password, BCrypt)
- OpenAPI 3 (Swagger UI)

## Run (Docker)
```bash
docker-compose up --build
```

## Swagger
- UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## Environment variables
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_ISSUER` (optional)
- `JWT_TTL_MINUTES` (optional)
- `TELEGRAM_BOT_TOKEN` (required for Telegram auth/notifications)
- `TELEGRAM_AUTH_MAX_AGE_SECONDS` (optional, default 300)
- `TELEGRAM_BOT_USERNAME`
- `TELEGRAM_MINI_APP_NAME`

## Telegram Mini App auth
The frontend should send `initData` from the Telegram Mini App to the backend:
```
POST /api/v1/auth/telegram
{"initData":"query_id=...&user=...&auth_date=...&hash=..."}
```
The backend validates `initData` using the Telegram bot token (HMAC SHA-256) and issues a JWT.

Example `initData` (shortened):
```
query_id=AAH...&user=%7B%22id%22%3A12345%2C%22first_name%22%3A%22Alex%22%7D&auth_date=1717230000&hash=...
```

## Example flow
1. Register user A
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Alex","email":"alex@example.com","password":"StrongPass123"}'
```
2. Register user B
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Sam","email":"sam@example.com","password":"StrongPass123"}'
```
3. Create invite (as user A)
```bash
curl -X POST http://localhost:8080/api/v1/pairs/invite \
  -H "Authorization: Bearer <A_TOKEN>"
```
4. Join invite (as user B)
```bash
curl -X POST http://localhost:8080/api/v1/pairs/join \
  -H "Authorization: Bearer <B_TOKEN>" \
  -H 'Content-Type: application/json' \
  -d '{"code":"A1B2C3"}'
```
5. Send mood request (as user A)
```bash
curl -X POST http://localhost:8080/api/v1/mood-requests \
  -H "Authorization: Bearer <A_TOKEN>"
```
6. Answer mood request (as user B)
```bash
curl -X POST http://localhost:8080/api/v1/mood-requests/<REQUEST_ID>/answer \
  -H "Authorization: Bearer <B_TOKEN>" \
  -H 'Content-Type: application/json' \
  -d '{"baseFeeling":"OK","mode":"SUPPORT","avoid":["PRESSURE"],"notePreset":"MSG_1"}'
```
7. Get partner status (as user A)
```bash
curl -X GET http://localhost:8080/api/v1/mood-status/partner \
  -H "Authorization: Bearer <A_TOKEN>"
```

## Telegram flow example
1. User opens Telegram Mini App and frontend sends `initData` to `/api/v1/auth/telegram`.
2. Backend returns JWT; use it for API calls.
3. When user A sends a mood request, partner receives a Telegram message with a link:
`https://t.me/<BOT_USERNAME>/<MINI_APP>?requestId=<REQUEST_ID>`.
