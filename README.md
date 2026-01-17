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
