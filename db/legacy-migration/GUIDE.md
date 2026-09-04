# Legacy -> `public2` ma'lumot ko'chirish — to'liq qo'llanma

Eski tizim ma'lumotlarini (`5434/isemid`, schema **`public`**) yangi tizimning
Liquibase sxemasiga (`5434/isemid`, schema **`public2`**) ko'chirish.

Manba va nishon **bitta bazada** -> oddiy `INSERT ... SELECT`, `postgres_fdw`
yoki dump/restore kerak emas.

Qaror mantig'i: [`docs/legacy-migration/02-mapping-5434.md`](../../docs/legacy-migration/02-mapping-5434.md)
+ [`01-decisions-locked.md`](../../docs/legacy-migration/01-decisions-locked.md).

---

## 0. Asosiy tamoyillar

| # | Tamoyil |
|---|---|
| 1 | **HECH BIR QATOR YO'QOLMAYDI.** NOT NULL maydon bo'sh bo'lsa sentinel qiymat (`'—'` / `0` / `created_at` / sentinel tashkilot) qo'yiladi va `public2._migration_notes` ga qayd etiladi. Migratsiya to'xtamaydi. |
| 2 | Legacy `id` qiymatlari **saqlanadi** (FK'lar mos kelishi uchun). Sequence'lar oxirida `setval` bilan to'g'rilanadi. |
| 3 | `public` ga **hech narsa yozilmaydi** — faqat `SELECT`. |
| 4 | Har fayl **bitta tranzaksiya**: SQL xato -> butun fayl `ROLLBACK`, runner to'xtaydi. |
| 5 | `00-prep.sql` `public2` biznes jadvallarini **`TRUNCATE ... CASCADE`** qiladi -> to'plam **istalgan marta qayta yugurtiriladi**. |
| 6 | `timestamp` -> `timestamp with time zone`: **`Asia/Tashkent`** (Toshkent mahalliy vaqti). |

**Ko'chadi:** `organization, users, patient, pt_*, form058, fm058_location, form058_1,
fm0581_*, card (+ 161/174/175/205/tube), act (+ 153/154/156/223/224)`.

**Ko'chmaydi:**
- `act155` / `act155_detail` **DETALI** (yangi sxemada bunday jadval yo'q) —
  lekin `act` bazaviy qatori (`act_type='ACT155'` bilan) ko'chadi.
- `users.password`, `user_roles`, `user_local_roles` (SSO token rol biriktiradi).
- `analytic_*`, `form_report_*`, `token_revocations`, `cls_*` (yangi sxema seed'idan).

**Sentinel qatorlar** (`00-prep`/`40` yaratadi):
`organization` id=0 (uuid butun-nol), `form058` id=0. Bog'lanish uzilib qolgan
`card`/`act` shularga ishora qiladi. Legacy id'lar 1 dan boshlangani uchun to'qnashuv yo'q.

---

## 1. Talablar

| | Windows | Linux |
|---|---|---|
| `psql` / `pg_dump` | `C:\Program Files\PostgreSQL\17\bin\` | `apt install postgresql-client-17` (yoki `-15`) |
| Runner | `run.ps1` (PowerShell 5.1+) | `run.sh` (bash) |
| `public2` | Liquibase bilan qurilgan bo'lishi shart (yangi ilova bir marta ishga tushsa — tayyor) |
| Kirish | `postgres` / parol |

`.sql` fayllar OS'ga bog'liq **emas** — sof PostgreSQL. Faqat runner farq qiladi.

---

## 2. Fayllar tartibi

| # | Fayl | Jadvallar |
|---|---|---|
| 1 | `00-prep.sql` | note-log jadval + `public2` biznes jadvallarini TRUNCATE + sentinel `organization(0)` |
| 2 | `10-organization.sql` | `organization`, `organization_service_types` |
| 3 | `20-users.sql` | `users`, `users_organizations` |
| 4 | `30-patient.sql` | `patient`, `pt_address`, `pt_affiliation`, `pt_identifier` |
| 5 | `40-form058.sql` | `fm058_location`, `form058` (+ sentinel `form058(0)`) |
| 6 | `45-form058-1.sql` | `form058_1`, `fm0581_animal_owner` -> owner_* ustunlar, `fm0581_bitten_person` -> `form058_1_other_injured_person` |
| 7 | `50-card.sql` | `card`, `card_users` |
| 8 | `51-card161.sql` | `card161` + barcha bola jadvallari |
| 9 | `52-card174.sql` | `card174` + bola jadvallari **(⚠️ nom juftliklari — 5-bo'lim)** |
| 10 | `53-card175.sql` | `card175` + element jadvallari **(⚠️)** |
| 11 | `54-card205.sql` | `card205` + bola jadvallari **(⚠️)** |
| 12 | `55-card-tube.sql` | `card_tube` + bola jadvallari **(⚠️)** |
| 13 | `60-act.sql` | `act`, `act_users` (ACT155 bazaviy qatori ham) |
| 14 | `61-act-subtypes.sql` | `act153/154/156/223/224` + `*_detail` |
| 15 | `90-finalize.sql` | `form058.assigned_card_id`, barcha `setval(...)`, **validatsiya hisoboti** |
| —  | `95-fix-sequences.sql` | **faqat kerak bo'lganda** — ko'chirishдан keyin ilova `duplicate key ... _pkey` bersa. Xavfsiz, mustaqil, `00-prep` kerak emas. `90-finalize` bilan bir xil mantiq. |

**Tartib majburiy** (FK): organization -> users -> patient -> form058/form058_1 -> card -> act.

---

## 3. Backup (majburiy)

**Windows PowerShell:**
```powershell
& "C:\Program Files\PostgreSQL\17\bin\pg_dump.exe" -h localhost -p 5434 -U postgres -d isemid -Fc -f C:\Users\PC\isemid-backup.dump
```
**Linux:**
```bash
PGPASSWORD=parol pg_dump -h localhost -p 5434 -U postgres -d isemid -Fc -f ~/isemid-backup.dump
```

---

## 4. Sinov yugurishi (birinchi marta majburiy)

`00-prep` `public2` ni tozalaydi -> avval **nusxada** sinang.

### 4.1. Sinov bazasini backup'dan yaratish

**Windows:**
```powershell
$env:PGPASSWORD='parol'
& "C:\Program Files\PostgreSQL\17\bin\psql.exe"     -h localhost -p 5434 -U postgres -d postgres -c "CREATE DATABASE isemid_test;"
& "C:\Program Files\PostgreSQL\17\bin\pg_restore.exe" -h localhost -p 5434 -U postgres -d isemid_test --no-owner --no-privileges C:\Users\PC\isemid-backup.dump
```
**Linux:**
```bash
export PGPASSWORD=parol
psql       -h localhost -p 5434 -U postgres -d postgres -c "CREATE DATABASE isemid_test;"
pg_restore -h localhost -p 5434 -U postgres -d isemid_test --no-owner --no-privileges ~/isemid-backup.dump
```

### 4.2. Yugurtirish

**Windows:**
```powershell
$env:PGPASSWORD='parol'
cd C:\Users\PC\IdeaProjects\ses\isemid-demo\db\legacy-migration
.\run.ps1 -Db isemid_test -Psql "C:\Program Files\PostgreSQL\17\bin\psql.exe" *> migration-test.txt
```
`psql` PATH'da bo'lsa `-Psql` shart emas.

**Linux:**
```bash
cd db/legacy-migration
PGPASSWORD=parol ./run.sh localhost 5434 isemid_test postgres 2>&1 | tee migration-test.txt
```

Runner `Continue? [yes/NO]` so'raydi — `yes`.

### 4.3. Alohida bitta fayl (kerak bo'lsa)

```
psql -h localhost -p 5434 -U postgres -d isemid_test -v ON_ERROR_STOP=1 -X -f 40-form058.sql
```

---

## 5. Natijani tekshirish (`90-finalize` hisoboti)

Log oxirida 4 blok:

### 5.1. Qator sonlari `src` (public) vs `dst` (public2)
```
form058       |  6 |  7      <- dst = src + 1 (sentinel form058)
organization  |  2 |  3      <- dst = src + 1 (sentinel org)
card / act / patient ...     <- dst = src (yoki >=)
```
`dst < src` **bo'lmasligi** kerak.

### 5.2. `... yo'qolgan` bloki
```
form058 yo'qolgan   | 0
card yo'qolgan      | 0
...
```
**Hammasi 0 bo'lishi SHART** — bu "hech bir manba qatori tushib qolmadi" degani.

### 5.3. JOINED butunlik
```
card subtype yo'q                          | 0
act subtype yo'q (act155dan tashqari xato) | 0
```
**0 bo'lishi kerak.** (ACT155 lar bu hisobdan chiqarilgan — ular uchun subtype yo'qligi normal.)

### 5.4. note-log xulosasi
```
form058 | sentinel/fallback ishlatildi | 6
```
Sentinel/fallback ishlatilgan qatorlar. Batafsil:
```sql
SELECT * FROM public2._migration_notes ORDER BY source_table, source_id;
```

### 5.5. Qo'lda solishtirish (ixtiyoriy)
```sql
SELECT id, status, icd10_code, patient_nnuzb, patient_full_name
FROM public2.form058 WHERE id <> 0 ORDER BY id LIMIT 20;

SELECT id, status, mkb10code, source FROM public.form058 ORDER BY id LIMIT 20;
```

---

## 6. `52`-`55` nom juftliklarini tasdiqlash

`card174 / card175 / card205 / card_tube` **BAZA** jadvallarida legacy va yangi
ustun nomlari deyarli mos kelmaydi. Fayl ichida `-- ??` bilan belgilangan qatorlar —
mening **taxminim** (masalan `card_tube.notified_to -> received_by`).

Sinovdan keyin:
1. `public2.card_tube` dan 10-20 qatorni `public.card_tube` bilan yonma-yon solishtiring.
2. Noto'g'ri joyga tushgan qiymatlarni toping (masalan bir ustunда telefon o'rniga sana).
3. `-- ??` qatorini to'g'irlang yoki menga ayting.

`docs/legacy-migration/20-card.md` §4a/§5a/§6a/§7a — har juftlik "ishonch darajasi".

---

## 7. Haqiqiy bazaga qo'llash (cutover)

```
1. Eski tizimni to'xtatish (yangi yozuv kelmasin).
2. Backup:  pg_dump -Fc isemid  ->  saqlash (3-bo'lim).
3. isemid.public2 Liquibase bilan qurilgan/yangilangan ekanini tekshirish.
4. Yugurtirish (sinovdagi kabi, faqat -Db isemid_test  O'RNIGA  -Db isemid
   yoki -Db umuman bermay, default 'isemid'):
```
**Windows:**
```powershell
$env:PGPASSWORD='parol'
cd C:\Users\PC\IdeaProjects\ses\isemid-demo\db\legacy-migration
.\run.ps1 -Psql "C:\Program Files\PostgreSQL\17\bin\psql.exe" *> migration-real.txt
```
**Linux:**
```bash
cd db/legacy-migration
PGPASSWORD=parol ./run.sh localhost 5434 isemid postgres 2>&1 | tee migration-real.txt
```
```
5. 90-finalize hisoboti:
     - "yo'qolgan" bloki = hammasi 0
     - JOINED butunlik = 0
     - _migration_notes ko'rib chiqilgan
6. Ilovani public2 ga qaratib ishga tushirish, smoke-test.
7. public schema HALI SAQLANADI (1-2 hafta). Muammo bo'lsa orqaga qaytish oson.
8. Hammasi joyida bo'lsa:  DROP SCHEMA public CASCADE;
```

> ⚠️ `00-prep` CASCADE `isemid.public2` da quyidagilarni ham tozalaydi:
> `rp_form2/31/32_manual_entry`, `form_129`, `user_roles`, `user_local_roles`,
> `outbound_webhook_dispatch`. Haqiqiy cutover'da `public2` toza (bo'sh) bo'lishi
> kerak, shuning uchun bu muammo emas. Agar `public2` da yangi ilova orqali
> kiritilgan hisobot ma'lumoti bo'lsa — avval alohida saqlang.

---

## 8. Qayta yugurtirish

`00-prep` har safar `public2` ni tozalaydi -> to'plam **qayta yugurtiriladi**.
Bir fayl xato bersa:
1. Log'dan xato xabarini va `_migration_notes` ni ko'ring.
2. Tegishli `.sql` faylni to'g'irlang.
3. Runner'ni **boshidan** (`00-prep` dan) yugurtiring.

Yarim to'plamni qayta yugurtirish **mumkin emas** — `00-prep` hammasini tozalaydi,
har fayl faqat o'z jadvalini to'ldiradi. Har doim to'liq to'plam.

---

## 9. Muammolar

| Belgi | Sabab / yechim |
|---|---|
| PowerShell: `The string is missing the terminator` | `run.ps1` da non-ASCII belgi. Bizniki toza ASCII — eski nusxa qolgan bo'lsa yangisini oling. |
| `psql: command not found` | to'liq yo'l: `-Psql "C:\Program Files\PostgreSQL\17\bin\psql.exe"` |
| `NativeCommandError` / NOTICE'da to'xtash | `run.ps1` da `$ErrorActionPreference='Continue'` bor (yangi nusxa). NOTICE endi to'xtatmaydi. |
| `relation "public2.X" does not exist` | `public2` Liquibase bilan qurilmagan — avval ilovani ishga tushiring |
| `null value in column "..." violates not-null` | skriptda e'tibordan qolgan NOT NULL — xato matnini bering, sentinel qo'shaman |
| `value too long for type character varying(N)` | legacy qiymat yangi ustundan uzun — jadval/ustunni ayting, `left(x,N)` qo'shaman |
| `insert or update ... violates foreign key` | tartib buzilgan yoki `00-prep` to'liq ishlamagan — boshidan |
| `duplicate key value violates unique constraint "<jadval>_pkey"` ilovada, ko'chirishдан keyin | Identity sequence 1 da qolgan (eski `90-finalize` `deptype='a'` filtri identity sequence'larni topmagan). Yechim: `95-fix-sequences.sql` ni yugurting (yoki yangilangan `90-finalize` bilan to'plamни qayta). |
| `_migration_notes` da ko'p qator | 5.4 — sentinel/fallback ishlatilgan qatorlar; ko'rib chiqing, kerak bo'lsa qoidani yumshataman |
| kirill harflari log'da buzuq (`ð│ð░`) | kosmetik — psql chiqishi UTF-8, terminal codepage'i boshqa. `chcp 65001` yordam beradi. Ma'lumotga ta'siri yo'q. |

---

## 10. Sinov natijasi (2026-08-28, `isemid_test`)

Muvaffaqiyatli. 6 form058, 2 card, 2 act, 6 patient, 1 card161, 1 card174,
1 act154, 1 act224 — hammasi ko'chdi. `yo'qolgan` = 0, JOINED butunlik = 0.
6 form058 uchun `NNUZB` yo'q edi (test bemorlarda `REO`) -> REO qiymati
`patient_nnuzb` ga yozildi, qator ko'chdi.
