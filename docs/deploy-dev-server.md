# Dev serverda ilovani ishga tushirish

Ilova — bitta executable Spring Boot jar (`target/app.jar`). Dev serverda `dev` profili bilan ishlaydi.
Parollar/kalitlar bu faylda **yo'q** — ular faqat `.env` da turadi (jamoaning secret store'idan olinadi).

## 1. Kerakli dasturlar

| Nima | Versiya | Izoh |
|---|---|---|
| **Java (JDK/JRE)** | **25** | `pom.xml` → `<java.version>25</java.version>`. Java 21 da **ishlamaydi** (jar 25 uchun kompilyatsiya qilingan). Masalan Temurin 25 |
| Maven | 3.9+ | faqat jar yig'ish uchun (serverda kerak emas, agar jar tayyor kelsa) |
| PostgreSQL | 15+ (loyihada 17 ishlatilgan) | bitta baza `isemid`, schema `public2` |

Tekshirish:

```bash
java -version    # 25.x chiqishi kerak
```

## 2. Jar yig'ish

```bash
mvn clean package -DskipTests
```

Natija: `target/app.jar` (`<finalName>app</finalName>`).

## 3. `.env` — qayerda turishi kerak

Ilova `.env` ni **joriy ishchi papkadan (working directory)** o'qiydi (`springboot4-dotenv`). Ya'ni `.env` jar ishga tushirilgan papkada bo'lishi shart:

```
<ish papkasi>/
├── app.jar
└── .env          ← shu yerda (gitignored, commitga kirmaydi)
```

- Dev serverda: `/srv/project/backend/.env` (servis aynan shu papkadan ishga tushadi).
- Haqiqiy environment variable `.env` dagi qiymatdan **ustun** turadi (systemd `Environment=` / `export` bersa, `.env` ni bosib ketadi).
- Namuna: repo'dagi [`.env.example`](../.env.example) — nusxa olib to'ldiriladi.
- `SPRING_PROFILES_ACTIVE=dev` ham `.env` ichida (yoki `--spring.profiles.active=dev` argumenti bilan).

## 4. Ishga tushirish

Ish papkasiga o'tib (`.env` shu yerda bo'lsin):

```bash
cd /srv/project/backend
java -jar app.jar
```

Profilni argument bilan berish:

```bash
java -jar app.jar --spring.profiles.active=dev
```

Fonda (systemd'siz, tezkor):

```bash
nohup java -jar app.jar > app.log 2>&1 &
```

Xotira chegarasi kerak bo'lsa: `java -Xms512m -Xmx2g -jar app.jar`.

Servis sifatida (`backend.service`) — muhim joylari:

```ini
[Service]
WorkingDirectory=/srv/project/backend     # .env shu yerdan o'qiladi
ExecStart=/usr/bin/java -jar /srv/project/backend/app.jar
Restart=on-failure
```

## 5. Database sozlamasi

Bitta PostgreSQL bazasi, ikki schema: `public2` — yangi tizim (Liquibase boshqaradi, ilova shunga ulanadi), `public` — eski legacy ma'lumot (faqat migratsiya manbasi).

`.env` da:

```properties
DB_URL=jdbc:postgresql://localhost:5434/isemid?currentSchema=public2
DB_USERNAME=postgres
DB_PASSWORD=<secret store'dan>
DB_POOL_MAX=20
DB_POOL_MIN_IDLE=5
```

- Format: `jdbc:postgresql://<HOST>:<PORT>/isemid?currentSchema=public2`.
- `?currentSchema=public2` **shart** — usiz ilova legacy `public` schema'ga tushib qoladi.
- Schema'ni Liquibase o'zi yaratadi/yangilaydi (birinchi ishga tushishda). Qo'lda DDL kerak emas, `ddl-auto=validate`.
- Hujjatlardagi baza: `localhost:5434/isemid` (`5432` eskirgan).

## 6. `.env` dagi asosiy qiymatlar (dev)

```properties
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=18090
APP_BASE_URI=https://test-ykem.sanepid.uz/api
DOCS_DIR=/srv/project/backend/docs
EXPORT_STORAGE_DIR=/srv/project/backend/exports
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,https://test-ykem.sanepid.uz
APP_DEV_SEED_ENABLED=false

SSO_AUTH_BASE_DOMAIN=https://test-sso.ssv.uz
SSO_IAM_API_BASE_DOMAIN=https://test-api2.ssv.uz
SSO_ISSUER_URI=https://test-sso.ssv.uz
SSO_PUBLIC_KEY_URI=https://test-sso.ssv.uz/public-key
API2_BASE_URL=https://test-api2.ssv.uz
LIS_BASE_URL=https://test-lis.sanepid.uz/api/v1/publicapi
LIS_CALLBACK_BASE_URL=https://test-ykem.sanepid.uz/api
```

To'liq ro'yxat — `.env.example`. Secret qiymatlar (faqat `.env` ga): `DB_PASSWORD`, `DHP_M2M_CLIENT_SECRET`, `DHP_WEB_LOGIN_CLIENT_SECRET`, `LIS_API_KEY`, `MUXLISA_API_KEY`, `DEV_PANEL_ADMIN_BOOTSTRAP_PASSWORD` (>= 12 belgi) va **`WEBHOOK_SECRET_KEY`** (startup uchun majburiy: `openssl rand -base64 32`).

## 7. Tekshirish

```bash
curl -s http://localhost:18090/v1/actuator/health     # {"status":"UP"}
```

## 8. Tez-tez uchraydigan muammolar

| Alomat | Sabab / yechim |
|---|---|
| `UnsupportedClassVersionError` | Java 25 dan past versiya — Java 25 o'rnating |
| `.env` o'qilmayapti, `DB_URL` bo'sh | `java -jar` `.env` turgan papkadan ishga tushirilmagan |
| Startup'da `WEBHOOK_SECRET_KEY` xatosi | `.env` da kalit yo'q |
| `relation "..." does not exist` | `DB_URL` da `?currentSchema=public2` yo'q yoki noto'g'ri baza |
| Liquibase lock'da osilib qoladi | `public2.databasechangeloglock` da `locked=false` qiling |
| Frontend'da CORS xatosi | `APP_CORS_ALLOWED_ORIGINS` ga domen qo'shilmagan |
| 401 / token tekshirilmayapti | `SSO_ISSUER_URI` / `SSO_PUBLIC_KEY_URI` bo'sh yoki noto'g'ri |
