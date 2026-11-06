# Integration API — Authentication / Аутентификация / Autentifikatsiya

**Language / Язык / Til:** [O'zbekcha](#uz) · [Русский](#ru) · [English](#en)

Base path: `/integration/v1/**`. Endpoints: [form058](../form058/README.md) · [form0581](../form0581/README.md) · [form129](../form129/README.md) · [patient](../patient/README.md)

---

<a id="uz"></a>
# 🇺🇿 O'zbekcha

## 1. Nimalar beriladi

Integratsiya uchun biz sizga quyidagilarni beramiz:

- `clientId` (`ic_...` ko'rinishida) va autentifikatsiya usuliga mos **maxfiy qiymat** (bir marta ko'rsatiladi, uni xavfsiz saqlang);
- `sourceKey` — sizning tizimingiz nomi (masalan `labx`), URL dagi `{source}` o'rniga qo'yiladi;
- ruxsat etilgan amallar (scope): `form058:submit`, `form0581:submit`, `form129:submit`, `patient-case:read`;
- kerak bo'lsa, qaysi IP manzillardan murojaat qilishingiz mumkinligi.

Autentifikatsiya usuli sizga biriktirilgan bo'ladi — quyidagi 5 tadan **bittasi**:

| Usul | Nima yuborasiz |
|---|---|
| [A. Client credentials](#uz-a) | avval token olasiz, keyin `Authorization: Bearer <token>` |
| [B. API key](#uz-b) | `X-Api-Key: <kalit>` |
| [C. Basic auth](#uz-c) | `Authorization: Basic ...` |
| [D. IP allow-list](#uz-d) | hech narsa (server IP bo'yicha taniladi) |
| [E. SSO/DHP token](#uz-e) | SSO yoki DHP dan olingan M2M token |

## 2. Usullar

<a id="uz-a"></a>
### A. Client credentials (token olish)

**1-qadam — token so'rang** (bu yagona autentifikatsiyasiz endpoint):

```bash
curl -X POST https://HOST/integration/v1/oauth/token \
  -H "Content-Type: application/json" \
  -d '{"clientId":"ic_xxx","clientSecret":"SIZNING_SECRET","grantType":"client_credentials"}'
```

Javob:

```json
{ "accessToken": "eyJ...", "tokenType": "Bearer", "expiresIn": 900, "scope": "form058:submit form129:submit" }
```

- `grantType` doim `client_credentials`.
- Token `expiresIn` soniya (odatda 900 = 15 daqiqa) amal qiladi. Muddati tugagach yangisini oling; har so'rovda yangi token olish shart emas, tokenni qayta ishlating.
- Xato `clientId`, xato secret yoki o'chirilgan klient — bir xil `401`.

**2-qadam — tokenni ishlating:**

```bash
curl -X POST https://HOST/integration/v1/labx/form-129 \
  -H "Authorization: Bearer eyJ..." -H "Content-Type: application/json" -d @body.json
```

<a id="uz-b"></a>
### B. API key

Kalit formati: `<clientId>.<token>` (butun satrni o'zgartirmasdan yuboring).

```bash
curl -X POST https://HOST/integration/v1/labx/form-129 \
  -H "X-Api-Key: ic_xxx.yyyyyy" -H "Content-Type: application/json" -d @body.json
```

<a id="uz-c"></a>
### C. Basic auth

Login — `clientId`, parol — sizga berilgan secret.

```bash
curl -u "ic_xxx:SIZNING_PAROL" -X POST https://HOST/integration/v1/labx/form-129 \
  -H "Content-Type: application/json" -d @body.json
```

(Bu `Authorization: Basic base64("ic_xxx:SIZNING_PAROL")` bilan teng.)

<a id="uz-d"></a>
### D. IP allow-list

Hech qanday `Authorization` yoki `X-Api-Key` header **yubormang** — bo'lmasa bu usul ishlamaydi. So'rov sizning ro'yxatdan o'tgan server IP manzilingizdan kelishi kerak, boshqa IP dan `401` qaytadi.

```bash
curl -X POST https://HOST/integration/v1/labx/form-129 \
  -H "Content-Type: application/json" -d @body.json
```

<a id="uz-e"></a>
### E. SSO/DHP token (M2M)

SSO yoki DHP dan o'zingizning `client_credentials` tokeningizni olib, `Authorization: Bearer <token>` bilan yuborasiz (token olish tartibi SSO/DHP hujjatida).

```bash
curl -X POST https://HOST/integration/v1/labx/form-129 \
  -H "Authorization: Bearer <SSO_YOKI_DHP_TOKEN>" -H "Content-Type: application/json" -d @body.json
```

Bu usulda tanada **doim** `senderOrganizationUuid` va `submittedBy` bo'lishi shart (3.3-band).

## 3. Barcha usullar uchun umumiy qoidalar

**3.1 `{source}`.** URL dagi `{source}` sizga berilgan `sourceKey` ga teng bo'lishi shart (registr ahamiyatsiz). Aks holda `403`. Masalan `sourceKey = labx` bo'lsa: `/integration/v1/labx/form-058`.

**3.2 Scope.** Har endpoint o'z ruxsatini talab qiladi:

| Endpoint | Scope |
|---|---|
| `POST .../form-058` | `form058:submit` |
| `POST .../form-058-1` | `form0581:submit` |
| `POST .../form-129` | `form129:submit` |
| `GET .../patients/case` | `patient-case:read` |

Ruxsat yo'q bo'lsa — `403`.

**3.3 Tashkilot.** Forma qaysi tashkilot nomidan yuborilayotgani aniq bo'lishi kerak. Eng ishonchli yo'l — har doim ikkalasini yuborish:
- tanada `senderOrganizationUuid` (tashkilot UUID) va `submittedBy` — `{ "pinfl": "...", "fullName": "...", "position": "...", "phone": "..." }` (formani yuborgan xodim, audit uchun);
- header `X-Organization-Id: <o'sha UUID>`.

`GET .../patients/case` va DMED endpointlarida tashkilot tanadan olinmaydi, shuning uchun u yerda `X-Organization-Id` header majburiy. Sizga ruxsat berilmagan tashkilot UUID si yuborilsa — `403`.

**3.4 IP cheklov.** Agar sizga IP ro'yxati belgilangan bo'lsa, u har bir so'rovda (har qanday usulda) tekshiriladi; boshqa IP dan — `403`.

**3.5 Rate limit.** Autentifikatsiya urinishlari tezligi cheklangan — juda ko'p xato urinishdan so'ng `429`. `Retry-After` kutib, qayta urining; tokenni qayta ishlating.

## 4. Xatolar

| HTTP | Sabab |
|---|---|
| `401` | credential xato/yo'q; token muddati tugagan; D usulida IP mos kelmadi |
| `403` | scope yo'q; `{source}` mos emas; tashkilot ruxsat etilmagan; IP cheklov; klient o'chirilgan |
| `400` | so'rov tanasi validatsiyadan o'tmadi (`VALIDATION_FAILED`, xabarda sabab) |
| `429` | juda ko'p urinish |

## 5. Keyingi qadam

Autentifikatsiya ishlagach: [form058](../form058/README.md), [form0581](../form0581/README.md), [form129](../form129/README.md), [patient](../patient/README.md).

---

<a id="ru"></a>
# 🇷🇺 Русский

## 1. Что вам выдаётся

Для интеграции мы выдаём:

- `clientId` (вида `ic_...`) и **секрет** под ваш способ аутентификации (показывается один раз — храните надёжно);
- `sourceKey` — имя вашей системы (например `labx`), подставляется в URL вместо `{source}`;
- разрешённые действия (scope): `form058:submit`, `form0581:submit`, `form129:submit`, `patient-case:read`;
- при необходимости — список IP-адресов, с которых разрешены обращения.

Способ аутентификации за вами закреплён — **один** из 5:

| Способ | Что вы отправляете |
|---|---|
| [A. Client credentials](#ru-a) | сначала получаете токен, затем `Authorization: Bearer <token>` |
| [B. API key](#ru-b) | `X-Api-Key: <ключ>` |
| [C. Basic auth](#ru-c) | `Authorization: Basic ...` |
| [D. IP allow-list](#ru-d) | ничего (сервер опознаётся по IP) |
| [E. Токен SSO/DHP](#ru-e) | M2M-токен, полученный в SSO или DHP |

## 2. Способы

<a id="ru-a"></a>
### A. Client credentials (получение токена)

**Шаг 1 — запросите токен** (единственный эндпоинт без аутентификации):

```bash
curl -X POST https://HOST/integration/v1/oauth/token \
  -H "Content-Type: application/json" \
  -d '{"clientId":"ic_xxx","clientSecret":"ВАШ_SECRET","grantType":"client_credentials"}'
```

Ответ:

```json
{ "accessToken": "eyJ...", "tokenType": "Bearer", "expiresIn": 900, "scope": "form058:submit form129:submit" }
```

- `grantType` всегда `client_credentials`.
- Токен действует `expiresIn` секунд (обычно 900 = 15 минут). По истечении получите новый; запрашивать токен на каждый вызов не нужно — используйте его повторно.
- Неверный `clientId`, неверный секрет или отозванный клиент — одинаковый `401`.

**Шаг 2 — используйте токен:**

```bash
curl -X POST https://HOST/integration/v1/labx/form-129 \
  -H "Authorization: Bearer eyJ..." -H "Content-Type: application/json" -d @body.json
```

<a id="ru-b"></a>
### B. API key

Формат ключа: `<clientId>.<token>` (передавайте всю строку целиком).

```bash
curl -X POST https://HOST/integration/v1/labx/form-129 \
  -H "X-Api-Key: ic_xxx.yyyyyy" -H "Content-Type: application/json" -d @body.json
```

<a id="ru-c"></a>
### C. Basic auth

Логин — `clientId`, пароль — выданный вам секрет.

```bash
curl -u "ic_xxx:ВАШ_ПАРОЛЬ" -X POST https://HOST/integration/v1/labx/form-129 \
  -H "Content-Type: application/json" -d @body.json
```

(Эквивалентно `Authorization: Basic base64("ic_xxx:ВАШ_ПАРОЛЬ")`.)

<a id="ru-d"></a>
### D. IP allow-list

**Не отправляйте** `Authorization` и `X-Api-Key` — иначе способ не сработает. Запрос должен приходить с вашего зарегистрированного IP-адреса сервера; с другого IP вернётся `401`.

```bash
curl -X POST https://HOST/integration/v1/labx/form-129 \
  -H "Content-Type: application/json" -d @body.json
```

<a id="ru-e"></a>
### E. Токен SSO/DHP (M2M)

Получите свой `client_credentials`-токен в SSO или DHP (порядок — в документации SSO/DHP) и отправляйте `Authorization: Bearer <token>`.

```bash
curl -X POST https://HOST/integration/v1/labx/form-129 \
  -H "Authorization: Bearer <ТОКЕН_SSO_ИЛИ_DHP>" -H "Content-Type: application/json" -d @body.json
```

При этом способе в теле **всегда** обязательны `senderOrganizationUuid` и `submittedBy` (п. 3.3).

## 3. Общие правила для всех способов

**3.1 `{source}`.** `{source}` в URL должен совпадать с выданным вам `sourceKey` (регистр не важен), иначе `403`. Например, при `sourceKey = labx`: `/integration/v1/labx/form-058`.

**3.2 Scope.** Каждый эндпоинт требует своё разрешение:

| Эндпоинт | Scope |
|---|---|
| `POST .../form-058` | `form058:submit` |
| `POST .../form-058-1` | `form0581:submit` |
| `POST .../form-129` | `form129:submit` |
| `GET .../patients/case` | `patient-case:read` |

Нет разрешения — `403`.

**3.3 Организация.** Должно быть однозначно, от имени какой организации отправляется форма. Надёжнее всего всегда передавать оба признака:
- в теле `senderOrganizationUuid` (UUID организации) и `submittedBy` — `{ "pinfl": "...", "fullName": "...", "position": "...", "phone": "..." }` (сотрудник, отправивший форму, — для аудита);
- заголовок `X-Organization-Id: <тот же UUID>`.

В `GET .../patients/case` и DMED-эндпоинтах организация из тела не берётся, поэтому там заголовок `X-Organization-Id` обязателен. UUID недоступной вам организации — `403`.

**3.4 Ограничение по IP.** Если для вас задан список IP, он проверяется на каждом запросе (при любом способе); с другого IP — `403`.

**3.5 Лимиты.** Частота попыток аутентификации ограничена — после множества неудачных попыток вернётся `429`. Подождите (`Retry-After`) и повторите; переиспользуйте токен.

## 4. Ошибки

| HTTP | Причина |
|---|---|
| `401` | неверные/отсутствующие данные; токен истёк; для способа D не совпал IP |
| `403` | нет scope; не совпал `{source}`; организация не разрешена; ограничение по IP; клиент отключён |
| `400` | тело запроса не прошло валидацию (`VALIDATION_FAILED`, причина в сообщении) |
| `429` | слишком много попыток |

## 5. Дальше

Когда аутентификация работает: [form058](../form058/README.md), [form0581](../form0581/README.md), [form129](../form129/README.md), [patient](../patient/README.md).

---

<a id="en"></a>
# 🇬🇧 English

## 1. What you are given

For an integration we provide:

- a `clientId` (looks like `ic_...`) and a **secret** matching your authentication method (shown once — store it safely);
- a `sourceKey` — your system's name (e.g. `labx`), used in place of `{source}` in URLs;
- permitted actions (scopes): `form058:submit`, `form0581:submit`, `form129:submit`, `patient-case:read`;
- if applicable, the list of IP addresses you may call from.

Your authentication method is fixed — **one** of 5:

| Method | What you send |
|---|---|
| [A. Client credentials](#en-a) | get a token first, then `Authorization: Bearer <token>` |
| [B. API key](#en-b) | `X-Api-Key: <key>` |
| [C. Basic auth](#en-c) | `Authorization: Basic ...` |
| [D. IP allow-list](#en-d) | nothing (your server is recognised by its IP) |
| [E. SSO/DHP token](#en-e) | an M2M token obtained from SSO or DHP |

## 2. Methods

<a id="en-a"></a>
### A. Client credentials (obtain a token)

**Step 1 — request a token** (the only unauthenticated endpoint):

```bash
curl -X POST https://HOST/integration/v1/oauth/token \
  -H "Content-Type: application/json" \
  -d '{"clientId":"ic_xxx","clientSecret":"YOUR_SECRET","grantType":"client_credentials"}'
```

Response:

```json
{ "accessToken": "eyJ...", "tokenType": "Bearer", "expiresIn": 900, "scope": "form058:submit form129:submit" }
```

- `grantType` is always `client_credentials`.
- The token is valid for `expiresIn` seconds (normally 900 = 15 min). Fetch a new one when it expires; do not request a token per call — reuse it.
- Wrong `clientId`, wrong secret or a disabled client all return the same `401`.

**Step 2 — use the token:**

```bash
curl -X POST https://HOST/integration/v1/labx/form-129 \
  -H "Authorization: Bearer eyJ..." -H "Content-Type: application/json" -d @body.json
```

<a id="en-b"></a>
### B. API key

Key format: `<clientId>.<token>` (send the whole string unchanged).

```bash
curl -X POST https://HOST/integration/v1/labx/form-129 \
  -H "X-Api-Key: ic_xxx.yyyyyy" -H "Content-Type: application/json" -d @body.json
```

<a id="en-c"></a>
### C. Basic auth

Username is the `clientId`, password is the secret you were given.

```bash
curl -u "ic_xxx:YOUR_PASSWORD" -X POST https://HOST/integration/v1/labx/form-129 \
  -H "Content-Type: application/json" -d @body.json
```

(Equivalent to `Authorization: Basic base64("ic_xxx:YOUR_PASSWORD")`.)

<a id="en-d"></a>
### D. IP allow-list

**Do not send** `Authorization` or `X-Api-Key` — otherwise this method will not apply. The request must come from your registered server IP; any other IP gets `401`.

```bash
curl -X POST https://HOST/integration/v1/labx/form-129 \
  -H "Content-Type: application/json" -d @body.json
```

<a id="en-e"></a>
### E. SSO/DHP token (M2M)

Obtain your own `client_credentials` token from SSO or DHP (see their documentation for how) and send `Authorization: Bearer <token>`.

```bash
curl -X POST https://HOST/integration/v1/labx/form-129 \
  -H "Authorization: Bearer <SSO_OR_DHP_TOKEN>" -H "Content-Type: application/json" -d @body.json
```

With this method the body must **always** include `senderOrganizationUuid` and `submittedBy` (§3.3).

## 3. Rules common to every method

**3.1 `{source}`.** The URL `{source}` must equal the `sourceKey` you were given (case-insensitive), otherwise `403`. E.g. with `sourceKey = labx`: `/integration/v1/labx/form-058`.

**3.2 Scopes.** Each endpoint needs its own permission:

| Endpoint | Scope |
|---|---|
| `POST .../form-058` | `form058:submit` |
| `POST .../form-058-1` | `form0581:submit` |
| `POST .../form-129` | `form129:submit` |
| `GET .../patients/case` | `patient-case:read` |

Missing scope → `403`.

**3.3 Organization.** It must be unambiguous on whose behalf a form is sent. The safest approach is to always send both:
- in the body, `senderOrganizationUuid` (the organization's UUID) and `submittedBy` — `{ "pinfl": "...", "fullName": "...", "position": "...", "phone": "..." }` (the staff member who submitted, for audit);
- the header `X-Organization-Id: <the same UUID>`.

`GET .../patients/case` and the DMED endpoints do not read the organization from the body, so `X-Organization-Id` is mandatory there. An organization UUID you are not allowed to use → `403`.

**3.4 IP restriction.** If an IP list is configured for you, it is checked on every request (whatever the method); another IP → `403`.

**3.5 Rate limiting.** Authentication attempts are rate-limited — many failed attempts return `429`. Wait (`Retry-After`) and retry; reuse your token.

## 4. Errors

| HTTP | Cause |
|---|---|
| `401` | bad/missing credentials; expired token; for method D the IP did not match |
| `403` | missing scope; `{source}` mismatch; organization not allowed; IP restriction; client disabled |
| `400` | request body failed validation (`VALIDATION_FAILED`, reason in the message) |
| `429` | too many attempts |

## 5. Next

Once authentication works: [form058](../form058/README.md), [form0581](../form0581/README.md), [form129](../form129/README.md), [patient](../patient/README.md).
