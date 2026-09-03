# Mapping: tayanch jadvallar — `organization`, `users`, `patient`, `pt_*`

Manba qoidalar: `00-overview.md`. **NN** = yangi `NOT NULL`, **⚠️** = domen tasdig'i.
Bular `form058`/`card`/`act` dan **oldin** ko'chiriladi.

Umumiy: `id` saqlanadi · `version` → `0` · `created_at`/`updated_at` → `timestamptz`
(legacy `NULL` bo'lsa `COALESCE(x, now())`, chunki yangi'да NN) ·
`created_by_id`/`updated_by_id` legacy'да yo'q jadvallarда → `NULL`.

---

## 1. `public.organization` → `public2.organization`

| legacy | yangi | transform |
|---|---|---|
| `id` | `id` | saqlanadi |
| — | `version` NN? (nullable) | `0` |
| `created_at` (nullable) | `created_at` **NN** | `COALESCE(created_at, now())` → `timestamptz` |
| `updated_at` (nullable) | `updated_at` **NN** | `COALESCE(updated_at, now())` → `timestamptz` |
| — | `created_by_id` / `updated_by_id` | `NULL` |
| `active` NN | `active` (nullable) | to'g'ridan |
| `name` NN | `name` NN | to'g'ridan |
| — | `name_uz` / `name_ru` / `name_kaa` / `name_uz_cyril` | ⚠️ `name` ni `name_uz` ga ham nusxa qilinsinmi? yoki `NULL`? |
| `city_code` | `district_code` | nom o'zgardi (SOATO — ⚠️ format) |
| `state_code` | `region_code` | nom o'zgardi |
| `district` (erkin matn) | — | ⚠️ yangi'да yo'q — tashlanadimi yoki `district_code` ga? |
| `line` | `address_line` | nom o'zgardi |
| `level_type` (nullable) | `level_type` **NN** | legacy NULL → `'NOT_DEFINED'` |
| `medical_type` (nullable) | `medical_type` **NN** | legacy NULL → `'OTHER'` (MedicalType has no `NOT_DEFINED`) |
| `phone` | `phone` | to'g'ridan |
| `tin` | `tin` | to'g'ridan |
| `parent_id` | `parent_id` | to'g'ridan (self-FK — topologik tartib yoki deferrable) |
| `uuid` NN | `uuid` NN | to'g'ridan |
| `country_code` | — | ⚠️ yangi'да yo'q — DROP |
| `email` | — | ⚠️ yangi'да yo'q — DROP |
| `service_area_code` | — | ⚠️ yangi'да yo'q — DROP |

**`organization_service_types`:** `(organization_id, service_type)` — **1:1**.

---

## 2. `public.users` → `public2.users`

| legacy | yangi | transform |
|---|---|---|
| `id` | `id` | saqlanadi |
| — | `version` | `0` |
| `created_at` (nullable) | `created_at` **NN** | `COALESCE(x, now())` → `timestamptz` |
| `updated_at` (nullable) | `updated_at` **NN** | `COALESCE(x, now())` → `timestamptz` |
| — | `created_by_id` / `updated_by_id` | `NULL` |
| `active` NN | `active` | to'g'ridan |
| `birth_date` | `birth_date` | to'g'ridan |
| `first_name` / `last_name` / `middle_name` / `gender_code` | bir xil | to'g'ridan |
| `line` | `line` | to'g'ridan |
| `nnuzb` | `nnuzb` | to'g'ridan |
| `phone_number` | `phone_number` | to'g'ridan (legacy `varchar(255)` → yangi `varchar(20)` — ⚠️ uzunlik tekshiruvi) |
| `country_code` | `country_code` | to'g'ridan (⚠️ `varchar(20)` uzunlik) |
| `city_code` | `district_code` | nom o'zgardi (⚠️ `varchar(20)`) |
| `state_code` | `region_code` | nom o'zgardi (⚠️ `varchar(20)`) |
| `district` (erkin matn) | — | ⚠️ yangi'да yo'q — DROP |
| `uuid` NN | `uuid` NN | to'g'ridan |
| — | `username` (nullable) | ⚠️ nima bilan to'ldiriladi? `nnuzb`? `ppn`? `NULL`? |
| `password` | — | **DROP** (qaror — SSO/JWT) |
| `position_code` | — | ⚠️ yangi'да yo'q — DROP |
| `ppn` | — | ⚠️ DROP yoki `username` ga? |

**`users_organizations`:** `(user_id, organization_id)` — **1:1**.

**`user_roles`:** `(user_id, role_id)` — ⚠️ **role_id crosswalk kerak.** Yangi
`role` lar Liquibase seed'idan keladi (aniq `id` bilan), legacy `role.id` bilan
mos kelmasligi mumkin. Legacy `role.name` ↔ yangi `role.name` bo'yicha
crosswalk qilinadi. Yangi'да mos rol yo'q bo'lsa — o'sha qator tashlanadi + hisobot.

**`user_local_roles`:** yangi jadval, legacy'да yo'q → ko'chirilmaydi.

---

## 3. `public.patient` → `public2.patient`

**Deyarli 1:1.** Barcha ustunlar bir xil nom:
`id, created_at, created_by_id, created_org_uuid, updated_at, updated_by_id,
updated_org_uuid, uuid, age_months, age_years, birth_date, category_code,
first_name, gender_code, kinship_degree, kinship_full_name, last_name,
marital_status_code, middle_name, phone_number, population_type_code,
profession_code, residential_status_code`.

Transform: `+ version = 0`, `created_at`/`updated_at` → `timestamptz` (legacy NN).

---

## 4. `pt_*` jadvallari

### `public.pt_address` → `public2.pt_address`

| legacy | yangi | transform |
|---|---|---|
| `id`, audit, `uuid` | bir xil | `+version`, `timestamptz` |
| `apartment_number` / `house_number` / `neighborhood_code` / `street_address` / `type` | bir xil | to'g'ridan (`type` CHECK: PERMANENT/TEMPORARY — bir xil) |
| `city_code` | `district_code` | nom o'zgardi |
| `state_code` | `region_code` | nom o'zgardi |
| `patient_id` (nullable) | `patient_id` **NN** | ⚠️ legacy NULL qatorlar → tashlanadi yoki QAROR |
| — | `created_org_uuid` / `updated_org_uuid` | `NULL` |

### `public.pt_affiliation` → `public2.pt_affiliation`

| legacy | yangi | transform |
|---|---|---|
| `id`, audit, `uuid` | bir xil | `+version`, `timestamptz` |
| `address` / `last_visited_date` / `organization_id` / `organization_name` / `type` | bir xil | to'g'ridan (`type` CHECK: WORKPLACE/EDUCATIONAL) |
| `city_code` | `district_code` | nom o'zgardi |
| `state_code` | `region_code` | nom o'zgardi |
| `patient_id` (nullable) | `patient_id` **NN** | ⚠️ legacy NULL → tashlanadi yoki QAROR |
| — | `organization_uuid` | `organization_id` bo'yicha `public.organization.uuid` dan lookup (NULL bo'lsa NULL) |
| — | `created_org_uuid` / `updated_org_uuid` | `NULL` |

### `public.pt_identifier` → `public2.pt_identifier`

| legacy | yangi | transform |
|---|---|---|
| `id`, audit, `uuid` | bir xil | `+version`, `timestamptz` |
| `period_start` / `period_end` / `value` NN | bir xil | to'g'ridan |
| `type_code` (nullable) | `type_code` **NN** (varchar30) | ⚠️ legacy NULL qatorlar → QAROR (default `'UNKNOWN'`?) |
| `patient_id` NN | `patient_id` NN | to'g'ridan |
| — | `created_org_uuid` / `updated_org_uuid` | `NULL` |

---

## 5. ⚠️ Ushbu modul uchun ochiq qarorlar

1. **`organization` / `users`: `city_code`→`district_code`, `state_code`→`region_code`** —
   legacy SOATO kod formati yangi seed (`ref_district`/`ref_region`) bilan bir xilmi?
   Farqli bo'lsa — value transform + crosswalk jadval (`00` §3, `docs/legacy-data-migration.md` §4).
2. **`organization.district` / `users.district`** (erkin matn) — tashlanadimi yoki `district_code` ga urinib ko'riladimi?
3. **`organization.name` → `name_uz`** ham nusxa qilinsinmi (ko'p tilli ustunlar)?
4. ~~**`organization.level_type` / `medical_type` NULL** qatorlar — nima qiymat (NN!)?~~
   HAL QILINDI: `level_type`→`'NOT_DEFINED'`, `medical_type`→`'OTHER'` (MedicalType'da `NOT_DEFINED` yo'q).
5. **`users.username`** — nima bilan to'ldiriladi (`nnuzb`? `ppn`? NULL)?
6. **`users.ppn` / `position_code` / `organization.country_code` / `email` / `service_area_code`** —
   tasdiqlangan DROP; e'tiroz bo'lsa hozir aytilsin.
7. **`user_roles` role_id crosswalk** — legacy↔yangi `role` moslashuvi (name bo'yicha?).
8. **`pt_address` / `pt_affiliation` `patient_id IS NULL`** legacy qatorlar (yangi NN) —
   tashlanadimi (yetim manzil) yoki QAROR?
9. **`pt_identifier.type_code IS NULL`** legacy qatorlar (yangi NN).
10. **`users.phone_number`** legacy `varchar(255)` → yangi `varchar(20)`; `region_code`/`district_code`
    `varchar(20)` — uzunlikdan oshган qiymatlar hisoboti.
