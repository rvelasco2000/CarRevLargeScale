# User API Guide

This document describes how to create a user and how to update a user's email or password.

## Create User

Use the register endpoint to create a new account.

```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "yourUsername",
    "email": "you@example.com",
    "password": "yourPassword"
  }'
```

The response returns `accessToken` and `refreshToken`.

## Update Email

Email updates require an access token and the current password.

```bash
curl -X POST "http://localhost:8080/api/user/changeEmail" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -d '{
    "newEmail": "new@example.com",
    "currentPassword": "yourPassword"
  }'
```

## Update Password

Password updates require an access token and the current password.

```bash
curl -X POST "http://localhost:8080/api/user/changePassword" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -d '{
    "currentPassword": "yourPassword",
    "newPassword": "yourNewPassword"
  }'
```

## Get Access Token

If you don’t have an access token yet, login first:

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "yourUsername",
    "password": "yourPassword"
  }'
```

Use the returned `accessToken` as the Bearer token in the update endpoints.
