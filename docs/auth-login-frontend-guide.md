# Signing in via SSO / DHP — frontend guide

How the frontend gets an access token for the end user, using the
backend's login-proxy endpoint. Full architecture details are in
[`auth-login-module.md`](./auth-login-module.md); this document is only
about what a client-side integration needs to know.

There are **two** endpoints, shared by every provider: one to exchange an
authorization code for a token, one to refresh an existing token. Both
providers today (`sso-web`, `dhp-web`) use the same scheme -
**Authorization Code + PKCE** (redirect to the provider's own login page,
no login/password form on our site).

Available in Swagger UI as its own group, **"Аутентификация"**
(`/swagger-ui.html`, group dropdown → "Аутентификация").

```
POST /v1/auth/login/{provider}     - exchange an authorization code for a token
POST /v1/auth/refresh/{provider}   - exchange a refresh token for a new token
Content-Type: application/json
```

No authentication required — both endpoints are public (they're the entry
points into the system).

## CORS

This endpoint is meant to be called directly from browser JavaScript
(`fetch`/`XHR`), so it needs to be reachable cross-origin. The backend's
allowed origins are configured server-side (`app.cors.allowed-origins`) -
in dev that's `http://localhost:3000` by default. **If your frontend runs
on a different origin/port, ask the backend team to add it** - otherwise
the browser will block the request before it even reaches the server (no
error will show up in the network tab as a normal HTTP response; it shows
as a CORS error in the console instead). No special `credentials`/cookie
handling is needed on the frontend side - this API authenticates via
`Authorization: Bearer`, not cookies, so a plain `fetch(url, { method:
"POST", ... })` without `credentials: "include"` is exactly right.

---

## How it works (identical for `sso-web` and `dhp-web`)

The user **does not enter a login/password on our site** — instead:

1. The frontend generates a PKCE pair (`code_verifier` + `code_challenge`,
   `S256`) and redirects the user's browser **directly to the provider's
   login page** (its `authorization_endpoint`, with query parameters
   `client_id`, `redirect_uri`, `code_challenge`,
   `code_challenge_method=S256`, `response_type=code`, etc. — the frontend
   can use the public `client_id` directly, it isn't secret).
2. The user logs in **on the provider's page**, not ours.
3. The provider redirects back to the given `redirect_uri` with a `?code=...`
   query parameter.
4. The frontend picks up that `code` from the URL and sends it to
   `POST /v1/auth/login/{provider}` together with the same `code_verifier`
   (generated in step 1) and the same `redirect_uri` (must match exactly
   what was used in step 1):

```
POST /v1/auth/login/sso-web
Content-Type: application/json

{
  "code": "abc123...",
  "codeVerifier": "the same code_verifier used in step 1",
  "redirectUri": "https://your-frontend/callback"
}
```

| Field | Type | Required |
|---|---|---|
| `code` | string | yes |
| `codeVerifier` | string | yes |
| `redirectUri` | string | yes — must match byte-for-byte what was sent in step 1 |

---

## 1. `sso-web`

Status: **verified live** against the real `test-sso.ssv.uz` — the real
`client_id` is recognized by the server (a direct request with a fake code
returned "code can't be decrypted", not "unknown client" — meaning the
client is configured correctly). No need to send `client_secret` — this
client is public, it doesn't have a secret at all.

## 2. `dhp-web`

Status: **verified live, working** — initially configured against the
wrong server (`playground.dhp.uz`, where this client_id/secret returned
`invalid_client`); the real server for this client is `dev.dhp.uz`. After
fixing `token-url` to `https://dev.dhp.uz/sso/oauth/token`, a direct
request with a fake code returned `invalid_grant` (client accepted, only
the test code rejected) - meaning the client_id/client_secret are working.
Unlike `sso-web`, this client **does** require sending `client_secret`
(despite being called a "public spa") - that's already handled on the
backend, nothing extra for the frontend to pass; `client_secret` stays
entirely on the backend.

---

## Refreshing a token

When `accessToken` expires (or before it does, proactively, using
`expiresIn`), exchange the `refreshToken` from the last successful
response for a new one - no redirect, no PKCE, no user interaction needed:

```
POST /v1/auth/refresh/sso-web
Content-Type: application/json

{
  "refreshToken": "def502..."
}
```

Returns the same response shape as `/login` (see below).

**Important - some providers rotate the refresh token on every use.**
DHP does (confirmed in its own API spec): every successful refresh
returns a **new** `refreshToken`, and the one you just sent stops working.
**Always overwrite your stored `refreshToken` with the value from the
latest response** - reusing an old one (even one that worked a moment
ago) will fail. This applies to both `sso-web` and `dhp-web`; don't assume
the refresh token you started with stays valid forever.

A missing `refreshToken` gets `400 VALIDATION_FAILED`, same shape as a
missing `code` on `/login`. Provider-not-found, upstream-error, and
timeout behavior are identical to `/login` too (see the error table
below) - the only practical difference between the two endpoints is what
you send in the body.

---

## Common response shape (any provider, either endpoint)

```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "def502...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "scope": "openid"
}
```

| Field | Type | Note |
|---|---|---|
| `accessToken` | string | Use as `Authorization: Bearer <accessToken>` on every subsequent API call. |
| `refreshToken` | string \| null | The provider may not return it at all — check for `null`/absence. When present, store it and send it to `POST /v1/auth/refresh/{provider}` to get a new token later - see "Refreshing a token" above. Some providers rotate it on every refresh; always overwrite your stored value with the latest response's. |
| `tokenType` | string | Always `"Bearer"`. |
| `expiresIn` | number \| null | `accessToken`'s lifetime in seconds. The provider may not send it — then it's `null`; treat a `401` from the API as the signal to re-authenticate. |
| `scope` | string \| null | The scopes granted (space-separated), if the provider sent any. |

**Using the token:** `accessToken` is the same JWT the provider would have
issued directly, so it works as an ordinary `Authorization: Bearer` on
every other endpoint of the platform — no extra exchange needed, whether it
came from `sso-web` or `dhp-web`.

## Errors

All errors use the same shape (`ErrorResponse`) as the rest of the API:

```json
{
  "success": false,
  "code": "UNAUTHORIZED",
  "message": "Tizimga kirib bo'lmadi. Iltimos, qayta urinib ko'ring.",
  "traceId": "c44fad7d48a8cdd8761950a267ab2454",
  "path": "/v1/auth/login/sso-web",
  "timestamp": "2026-07-28T10:25:36.044+05:00",
  "violations": []
}
```

`message` is localized for the request's current locale — safe to show to
the user directly.

| HTTP | `code` | When | What the frontend should do |
|---|---|---|---|
| `400` | `VALIDATION_FAILED` | `/login`: `code`/`codeVerifier`/`redirectUri` wasn't sent. `/refresh`: `refreshToken` wasn't sent. | Show a validation error on the relevant field/step. |
| `401` | `UNAUTHORIZED` | `/login`: the provider rejected the `code` (expired, already used, `redirect_uri`/`code_verifier` mismatch). `/refresh`: the provider rejected the `refreshToken` (expired, revoked, already rotated away). Either endpoint **or** the client's own credentials being rejected (see the DHP note above) — the frontend can't tell these apart from the response code, all of them return the same `401`. | `/login`: show "couldn't sign in, please try again". `/refresh`: fall back to the full redirect flow (the refresh token is no longer usable). If this happens systematically (not a one-off), escalate to the backend team — the problem may not be the specific user attempt. |
| `404` | `NOT_FOUND` | `{provider}` doesn't exist or is disabled. | A frontend bug — shouldn't happen normally. |
| `502` | `UPSTREAM_ERROR` | The provider returned an unexpected error (5xx or something non-standard). | Show "service temporarily unavailable, try again later". Can retry once with a delay. |
| `504` | `UPSTREAM_TIMEOUT` | The provider didn't respond in time. | Same as `502` — a transient issue, not a user-input error. |

## Examples (fetch)

```javascript
async function exchangeCode(provider, code, codeVerifier, redirectUri) {
  const response = await fetch(`/v1/auth/login/${provider}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ code, codeVerifier, redirectUri }),
  });

  const body = await response.json();
  if (!response.ok) {
    throw new Error(body.message);
  }
  return body; // body.accessToken -> store and use as the Bearer token
  // body.refreshToken -> store too, overwriting any previous value
}

async function refreshToken(provider, refreshToken) {
  const response = await fetch(`/v1/auth/refresh/${provider}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });

  const body = await response.json();
  if (!response.ok) {
    // 401 here means the refresh token itself is no longer valid -
    // fall back to the full redirect flow, don't retry this call.
    throw new Error(body.message);
  }
  return body; // body.refreshToken may differ from what was sent - overwrite the stored value
}

// After the redirect back with ?code=...
await exchangeCode("sso-web", code, codeVerifier, redirectUri);
await exchangeCode("dhp-web", code, codeVerifier, redirectUri);

// Later, to refresh:
await refreshToken("sso-web", storedRefreshToken);
await refreshToken("dhp-web", storedRefreshToken);
```

## Important: what these endpoints don't have yet

- **Login via username/password (without a redirect to the provider's
  page) is no longer supported** — that flow existed (`sso`, password
  grant) and was removed after moving to a single redirect-based approach.
