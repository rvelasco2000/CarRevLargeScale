# Authentication – Basic Flow & Testing Guide

This document describes the **authentication system**, its **basic flow**, and **how to test it manually** using `curl`.

The authentication module is implemented using:
- JWT (Access & Refresh Tokens)
- MongoDB (user storage)
- Redis (refresh token revocation)
- Spring Security

---

## 1. Architecture Overview

| Component | Responsibility |
|---------|----------------|
| MongoDB | Persistent user storage |
| Redis | Store and revoke refresh tokens |
| JWT Access Token | Stateless authentication |
| JWT Refresh Token | Token renewal |
| Spring Security | Request filtering & authorization |

---

## 2. Authentication Flow

### 2.1 Register
1. Client sends `username + email + password`
2. Server:
   - Hashes password using BCrypt
   - Stores user in MongoDB
   - Generates access & refresh tokens
   - Stores refresh token identifier (`jti`) in Redis

### 2.2 Login
1. Client sends `username + password`
2. Server:
   - Verifies credentials
   - Issues new access & refresh tokens
   - Stores refresh token in Redis

### 2.3 Access Protected APIs
1. Client sends request with header:
   ```
   Authorization: Bearer <accessToken>
   ```
2. Server:
   - Validates JWT
   - Extracts username and role (`isAdmin`)
   - Authorizes request

### 2.4 Refresh Token
1. Client sends refresh token
2. Server:
   - Validates JWT signature
   - Checks `jti` exists in Redis
   - Issues new access token

### 2.5 Logout
1. Client sends refresh token
2. Server:
   - Removes refresh token from Redis
   - Token cannot be reused

---

## 3. Base URL

```
http://localhost:8080/api/auth
```

---

## 4. API Endpoints

### 4.1 Register
```
POST /register
```

```json
{
  "username": "khai",
  "email": "khai@test.com",
  "password": "123456"
}
```

**Response – 200 OK**
```json
{
  "accessToken": "JWT_ACCESS_TOKEN",
  "refreshToken": "JWT_REFRESH_TOKEN"
}
```

---

### 4.2 Login
```
POST /login
```

```json
{
  "username": "khai",
  "password": "123456"
}
```

**Response – 200 OK**
```json
{
  "accessToken": "JWT_ACCESS_TOKEN",
  "refreshToken": "JWT_REFRESH_TOKEN"
}
```

---

### 4.3 Refresh Token
```
POST /refresh
```

```json
{
  "refreshToken": "JWT_REFRESH_TOKEN"
}
```

**Response – 200 OK**
```json
{
  "accessToken": "NEW_JWT_ACCESS_TOKEN",
  "refreshToken": "SAME_REFRESH_TOKEN"
}
```

---

### 4.4 Logout
```
POST /logout
```

```json
{
  "refreshToken": "JWT_REFRESH_TOKEN"
}
```

**Response – 204 No Content**

---

## 5. Manual Testing (curl)

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"khai","email":"khai@test.com","password":"123456"}'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"khai","password":"123456"}'
```

### Refresh
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<REFRESH_TOKEN>"}'
```

### Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<REFRESH_TOKEN>"}'
```

---

## 6. End-to-End Test Checklist

1. Register → HTTP 200  
2. Login → HTTP 200  
3. Refresh → HTTP 200  
4. Logout → HTTP 204  
5. Refresh after logout → FAIL (401 / 403)

---

## 7. Security Rules

| Endpoint | Access |
|--------|--------|
| `/api/auth/register` | Public |
| `/api/auth/login` | Public |
| `/api/auth/refresh` | Public |
| `/api/auth/logout` | Public |
| Other endpoints | Require access token |

---

## 8. Common Troubleshooting

### 403 on `/refresh`
- Ensure `/api/auth/refresh` is marked as `permitAll`
- Refresh does not require `Authorization` header

### Application fails to start
- JPA enabled but no SQL datasource configured
- Disable JPA auto-configuration or add H2

### Refresh token always invalid
- Check Redis is running
- Check refresh token `jti` exists in Redis
- Ensure token was not already revoked

---

## 9. Status

✔ Authentication implemented  
✔ Manual testing completed  
✔ Ready for integration with domain APIs
