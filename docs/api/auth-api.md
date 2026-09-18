# Auth API

## POST /auth/register
Request: `{ "email": "...", "password": "...", "displayName": "..." }`
Response `201`: `{ "userId": "...", "accessToken": "...", "refreshToken": "..." }`
Errors: `EMAIL_TAKEN`, `WEAK_PASSWORD`

## POST /auth/login
Request: `{ "email": "...", "password": "..." }`
Response `200`: `{ "accessToken": "...", "refreshToken": "...", "expiresIn": 900 }`
Errors: `INVALID_CREDENTIALS`

## POST /auth/refresh
Request: `{ "refreshToken": "..." }`
Response `200`: new token pair. Refresh tokens rotate on every use.

## POST /auth/logout
Revokes the presented refresh token. Always returns `204`.

Access tokens are JWTs, 15 minute TTL. Refresh tokens last 30 days.
