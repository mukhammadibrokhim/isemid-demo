# Словарь данных (Data Dictionary): Form058 и Form058-1

Справочник по всем полям, используемым в модулях `form058` (Форма №058 —
экстренное извещение об инфекционном/паразитарном заболевании) и `form0581`
(Форма №058-1 — экстренное извещение об укусе/царапине/ослюнении животным,
подозрительным на бешенство).

Источник: `uz.uzinfocom.app.modules.form058.domain.model.*` и
`uz.uzinfocom.app.modules.form0581.domain.model.*`.

Обозначения в столбце «Обязательное»: **Да** — `nullable = false` в JPA
(значение всегда должно присутствовать), **Нет** — поле может быть `null`.

---

## 1. Form058 (таблица `form058`)

### 1.1 Общие аудиторские поля (унаследованы от `BaseEntity` → `AuditableEntity` →
`UuidAuditableEntity` → `OrganizationScopedEntity` → `AbsEntity`)

Эти поля присутствуют во всех сущностях-агрегатах платформы, включая `Form058` и `Form0581`.

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `id` | `id` | `Long` | Да | Первичный ключ записи, генерируется автоматически (IDENTITY). |
| `version` | `version` | `Long` | Нет | Версия записи для оптимистичной блокировки (JPA `@Version`). |
| `uuid` | `uuid` | `UUID` | Да | Глобально уникальный идентификатор записи, генерируется при первом сохранении (`@PrePersist`), используется во внешних интеграциях вместо числового `id`. |
| `createdAt` | `created_at` | `Instant` | Да | Дата и время создания записи, заполняется автоматически. |
| `updatedAt` | `updated_at` | `Instant` | Да | Дата и время последнего изменения записи, заполняется автоматически. |
| `createdBy` | `created_by_id` | `Long` | Нет | Идентификатор пользователя, создавшего запись. |
| `updatedBy` | `updated_by_id` | `Long` | Нет | Идентификатор пользователя, последним изменившего запись. |
| `createdOrgUuid` | `created_org_uuid` | `UUID` | Да | UUID организации, в контексте которой запись была создана (организационная изоляция данных). |
| `createdOrg` | `created_org_uuid` (только чтение) | `Organization` | — | Связь (не отдельная колонка) на организацию-создателя, только для чтения. |
| `updatedOrgUuid` | `updated_org_uuid` | `UUID` | Нет | UUID организации, в контексте которой запись была последний раз изменена. |
| `updatedOrg` | `updated_org_uuid` (только чтение) | `Organization` | — | Связь (не отдельная колонка) на организацию последнего изменения, только для чтения. |

### 1.2 Собственные поля `Form058`

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `status` | `status` | `FormStatus` (enum) | Да | Текущий статус жизненного цикла формы: `NOT_APPROVED`, `SENT`, `RECEIVED`, `CARD_LINKED`, `APPROVED_PENDING`, `APPROVED`, `CANCELED`. |
| `source` | `source` | `String(20)` | Да | Источник создания формы (например SSO/DHP-портал или внешний интеграционный клиент). |
| `patient` | `patient_id` | `Patient` (FK) | Да | Пациент, к которому относится извещение. Каскадное удаление намеренно не используется — форма не должна управлять жизненным циклом пациента. |
| `senderOrganizationId` | `sender_organization_id` | `Long` | Да | Идентификатор организации-отправителя (медицинская организация, зарегистрировавшая случай). |
| `receiverOrganizationId` | `receiver_organization_id` | `Long` | Да | Идентификатор организации-получателя (санитарно-эпидемиологическая служба). |
| `sourceIntegrationClientId` | `source_integration_client_id` | `Long` | Нет | Идентификатор интеграционного клиента, через API которого форма была создана. `null` для форм, поданных через SSO/DHP. Используется для адресной отправки исходящего webhook об изменении статуса только тому клиенту, который подал форму. |
| `location` | `location_id` | `Form058Location` (FK) | Нет | Геолокация случая заболевания (см. раздел 1.4). Persist/merge каскадируются, удаление — намеренно нет. |
| `hasLinkedCards` | `has_linked_cards` | `boolean` | Да | Денормализованный флаг: есть ли у формы связанные карты (для быстрой фильтрации в списках). |
| `assignedCardId` | `assigned_card_id` | `Long` | Нет | **Устаревшее (`@Deprecated`)** поле — идентификатор единственной привязанной карты, сохранено только для обратной совместимости API/БД. Актуальный механизм — флаг `hasLinkedCards` и таблица связей в модуле `card`. |

### 1.3 Встроенные (`@Embedded`) блоки полей `Form058`

#### `diagnosisInfo` (`Form058DiagnosisInfo`)

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `icd10Code` | `icd10_code` | `String(20)` | Да | Код диагноза по МКБ-10 (ICD-10), установленный при первичном извещении. |
| `icd10Name` | `icd10_name` | `String(512)` | Да | Наименование диагноза по МКБ-10, соответствующее `icd10Code`. |
| `finalIcd10Code` | `final_icd10_code` | `String(20)` | Нет | Итоговый (уточнённый) код МКБ-10, устанавливается при утверждении (`approve`) формы получателем. |
| `finalIcd10Name` | `final_icd10_name` | `String(512)` | Нет | Наименование итогового диагноза, соответствующее `finalIcd10Code`. |
| `icd10UsageLimit` | `icd10_usage_limit` | `Integer` | Нет | Ограничение по количеству использований данного кода МКБ-10 (справочное/валидационное значение). |

#### `clinicalInfo` (`Form058ClinicalInfo`)

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `labConfirmation` | `lab_confirmation` | `Boolean` | Нет | Признак лабораторного подтверждения диагноза. |
| `hospitalPlaceId` | `hospital_place_id` | `Long` | Нет | Идентификатор места госпитализации/лечебного учреждения. |

#### `dateInfo` (`Form058DateInfo`)

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `admissionDate` | `admission_date` | `LocalDateTime` | Нет | Дата и время поступления пациента в медицинское учреждение. |
| `diseaseDate` | `disease_date` | `LocalDateTime` | Да | Дата начала заболевания. |
| `firstVisitDate` | `first_visit_date` | `LocalDateTime` | Да | Дата первого обращения пациента за медицинской помощью. |
| `diagnosisDate` | `diagnosis_date` | `LocalDateTime` | Нет | Дата установления диагноза. |
| `visitDate` | `visit_date` | `LocalDateTime` | Да | Дата осмотра/визита, послужившего основанием для извещения. |
| `initialReportDateTime` | `initial_report_date_time` | `LocalDateTime` | Да | Дата и время подачи первичного экстренного извещения. |

#### `epidemicInfo` (`Form058EpidemicInfo`)

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `diseasePlaceCode` | `disease_place_code` | `String(64)` | Нет | Код места (условий) возникновения заболевания (справочное значение). |
| `diseaseCause` | `disease_cause` | `String(2000)` | Нет | Текстовое описание предполагаемой причины/источника заболевания. |
| `epidemicMeasures` | `epidemic_measures` | `String(2000)` | Нет | Описание принятых противоэпидемических мероприятий. |

#### `reportInfo` (`Form058ReportInfo`)

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `journalFormCode` | `journal_form_code` | `String(64)` | Да | Код формы журнала регистрации (справочник журналов). |
| `comment` | `form_comment` | `String(2000)` | Нет | Произвольный комментарий к форме. |
| `notifierFullName` | `notifier_full_name` | `String` | Да | ФИО лица, подавшего извещение. |
| `cardByFullName` | `card_by_full_name` | `String` | Нет | ФИО лица, заполнившего карту эпидемиологического обследования. |

#### `cancellationInfo` (`Form058CancellationInfo`)

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `cancelReason` | `cancel_reason` | `String(1000)` | Нет | Причина отмены (аннулирования) формы отправителем. |
| `canceledBy` | `canceled_by_id` | `Long` | Нет | Идентификатор пользователя, отменившего форму. |
| `canceledAt` | `canceled_at` | `Instant` | Нет | Дата и время отмены формы. |
| `notApprovedReason` | `not_approved_reason` | `String(1000)` | Нет | Причина отказа в утверждении формы получателем. |

#### `approvalInfo` (`Form058ApprovalInfo`)

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `approvedBy` | `approved_by_id` | `Long` | Нет | Идентификатор пользователя, утвердившего форму. |
| `approvedOrganizationId` | `approved_organization_id` | `Long` | Нет | Идентификатор организации, утвердившей форму (организация-получатель). |
| `approvedAt` | `approved_at` | `Instant` | Нет | Дата и время утверждения формы. |
| `approvedFullName` | `approved_full_name` | `String` | Нет | ФИО лица, утвердившего форму. |
| `approvedOrgUuid` | `approved_org_uuid` | `UUID` | Нет | UUID организации, утвердившей форму. |

#### `deleteInfo` (`Form058DeleteInfo`) — мягкое удаление

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `deleted` | `deleted` | `boolean` | Да | Признак мягкого удаления формы (`true` — запись считается удалённой, но физически остаётся в БД). |
| `deletedAt` | `deleted_at` | `Instant` | Нет | Дата и время удаления. |
| `deletedBy` | `deleted_by_id` | `Long` | Нет | Идентификатор пользователя, удалившего форму. |
| `deleteReason` | `delete_reason` | `String(1000)` | Нет | Причина удаления формы. |

### 1.4 Form058Location (таблица `fm058_location`)

Отдельная сущность геолокации, связанная с `Form058` через `location_id`
(`@OneToOne`, каскад только `PERSIST`/`MERGE`).

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `id`, `version`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy` | — | — | — | Стандартные аудиторские поля (см. `AuditableEntity`, раздел 1.1, без организационных и UUID-полей). |
| `latitude` | `latitude` | `Double` | Нет | Географическая широта места случая. |
| `longitude` | `longitude` | `Double` | Нет | Географическая долгота места случая. |
| `location` | `location` | `String(1000)` | Нет | Текстовое описание/адрес местоположения. |

### 1.5 Справочник `FormStatus`

| Значение | Описание |
|---|---|
| `NOT_APPROVED` | Форма подана, но не утверждена (например, отклонена получателем). |
| `SENT` | Форма отправлена получателю, ожидает получения/рассмотрения. |
| `RECEIVED` | Форма получена (принята) организацией-получателем. |
| `CARD_LINKED` | К форме привязана как минимум одна карта эпидемиологического обследования. |
| `APPROVED_PENDING` | Форма ожидает окончательного решения об утверждении. |
| `APPROVED` | Форма окончательно утверждена получателем. |
| `CANCELED` | Форма отменена (аннулирована) отправителем. |

---

## 2. Form0581 (таблица `form058_1`)

«Форма 058-1» — экстренное извещение о случае укуса/царапины/ослюнения
животным, подозрительным на бешенство. Самостоятельная форма (не наследник
`Form058`), с собственным статусом `Form0581Status`, но повторяющая ту же
модель жизненного цикла (создание/обновление/утверждение/отмена/удаление).

Общие аудиторские поля — те же, что в разделе 1.1 (`Form0581` тоже наследует
`AbsEntity`).

### 2.1 Собственные поля `Form0581`

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `status` | `status` | `Form0581Status` (enum) | Да | Текущий статус жизненного цикла формы (см. раздел 2.6). |
| `source` | `source` | `String(20)` | Да | Источник создания формы. |
| `patient` | `patient_id` | `Patient` (FK) | Да | Пациент — пострадавший (укушенный/оцарапанный) человек. |
| `senderOrganizationId` | `sender_organization_id` | `Long` | Да | Идентификатор организации-отправителя. |
| `receiverOrganizationId` | `receiver_organization_id` | `Long` | Да | Идентификатор организации-получателя. Ограничение (на уровне валидатора, не БД): получатель должен быть организацией с `medicalType == SANEPID_SERVICE`. |
| `sourceIntegrationClientId` | `source_integration_client_id` | `Long` | Нет | Идентификатор интеграционного клиента, подавшего форму через API (аналогично `Form058`). |
| `otherPeopleInjured` | `other_people_injured` | `Boolean` | Нет | Признак того, что в этом же инциденте пострадали и другие люди, помимо основного пациента. Управляет наличием записей в `otherInjuredPeople`. |
| `otherInjuredPeople` | — (`@OneToMany`, FK `form0581_id` в дочерней таблице) | `List<Form0581OtherInjuredPerson>` | — | Список прочих пострадавших в том же инциденте (см. раздел 2.7). |
| `hasLinkedCards` | `has_linked_cards` | `boolean` | Да | Денормализованный флаг наличия привязанных карт (аналог `Form058.hasLinkedCards`). |

### 2.2 Встроенные блоки полей `Form0581`

#### `diagnosisInfo` (`Form0581DiagnosisInfo`)

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `icd10Code` | `icd10_code` | `String(20)` | Да | Код диагноза по МКБ-10. |
| `icd10Name` | `icd10_name` | `String(512)` | Да | Наименование диагноза по МКБ-10. |
| `injuryLocalization` | `injury_localization` | `String(500)` | Нет | Локализация травмы (место укуса/царапины на теле). |
| `finalIcd10Code` | `final_icd10_code` | `String(20)` | Нет | Итоговый код МКБ-10, устанавливается при утверждении формы. |
| `finalIcd10Name` | `final_icd10_name` | `String(512)` | Нет | Наименование итогового диагноза. |

#### `incidentInfo` (`Form0581IncidentInfo`)

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `injuryDateTime` | `injury_date_time` | `LocalDateTime` | Да | Дата и время получения травмы (укуса/царапины). |
| `dpuVisitDateTime` | `dpu_visit_date_time` | `LocalDateTime` | Да | Дата и время обращения в травматологический/антирабический пункт (ДПУ). |
| `injuryRegionCode` | `injury_region_code` | `String(64)` | Да | Код региона (области), где произошёл инцидент. |
| `injuryDistrictCode` | `injury_district_code` | `String(64)` | Да | Код района, где произошёл инцидент. |
| `injuryAddress` | `injury_address` | `String(1000)` | Нет | Текстовый адрес места инцидента. |

#### `animalInfo` (`Form0581AnimalInfo`)

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `animalCategoryCode` | `animal_category_code` | `String(64)` | Нет | Код категории животного (справочник, например: домашнее/дикое/бродячее). |
| `animalColor` | `animal_color` | `String(255)` | Нет | Окрас животного. |
| `animalType` | `animal_type` | `String(255)` | Нет | Вид животного (собака, кошка и т.д.). |
| `animalBreed` | `animal_breed` | `String(255)` | Нет | Порода животного. |

#### `animalOwnerInfo` (`Form0581AnimalOwnerInfo`)

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `ownerLastName` | `owner_last_name` | `String(255)` | Нет | Фамилия владельца животного. |
| `ownerFirstName` | `owner_first_name` | `String(255)` | Нет | Имя владельца животного. |
| `ownerMiddleName` | `owner_middle_name` | `String(255)` | Нет | Отчество владельца животного. |
| `ownerAddress` | (см. ниже, с префиксом `owner_*`) | `Form0581Address` (embedded) | Нет | Адрес владельца животного (см. раздел 2.3). |

#### `hospitalizationInfo` (`Form0581HospitalizationInfo`)

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `hospitalizedAt` | `hospitalized_at` | `LocalDateTime` | Нет | Дата и время госпитализации пострадавшего. |
| `hospitalOrganizationId` | `hospital_organization_id` | `Long` | Нет | Идентификатор организации, в которую была произведена госпитализация. |

#### `reportInfo` (`Form0581ReportInfo`)

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `antirabicAssistanceInfo` | `antirabic_assistance_info` | `String(2000)` | Нет | Сведения об оказанной антирабической помощи (прививки, схема лечения). |
| `notifierFullName` | `notifier_full_name` | `String` | Да | ФИО лица, подавшего извещение. |
| `receiverFullName` | `receiver_full_name` | `String` | Нет | ФИО лица, принявшего извещение на стороне получателя. |
| `messageSentAt` | `message_sent_at` | `LocalDateTime` | Нет | Дата и время фактической отправки извещения. |

#### `cancellationInfo` (`Form0581CancellationInfo`)

Идентична по структуре `Form058CancellationInfo` (раздел 1.3): `cancelReason`
(`cancel_reason`), `canceledBy` (`canceled_by_id`), `canceledAt`
(`canceled_at`), `notApprovedReason` (`not_approved_reason`) — те же описания.

#### `approvalInfo` (`Form0581ApprovalInfo`)

Идентична по структуре `Form058ApprovalInfo` (раздел 1.3): `approvedBy`
(`approved_by_id`), `approvedOrganizationId` (`approved_organization_id`),
`approvedAt` (`approved_at`), `approvedFullName` (`approved_full_name`),
`approvedOrgUuid` (`approved_org_uuid`) — с той разницей, что здесь
окончательное решение об утверждении принимает организация-**отправитель**,
а не получатель (в отличие от `Form058`).

#### `deleteInfo` (`Form0581DeleteInfo`) — мягкое удаление

Идентична по структуре `Form058DeleteInfo` (раздел 1.3): `deleted`
(`deleted`), `deletedAt` (`deleted_at`), `deletedBy` (`deleted_by_id`),
`deleteReason` (`delete_reason`) — те же описания.

### 2.3 Form0581Address (переиспользуемый `@Embeddable`)

Общий блок «регион/район/махалля/улица/дом/квартира», используемый дважды:
в `animalOwnerInfo.ownerAddress` (с префиксом колонок `owner_*`) и в адресе
каждой записи `Form0581OtherInjuredPerson` (без префикса).

| Поле (Java) | Колонка (для владельца животного) | Колонка (для прочих пострадавших) | Тип | Описание |
|---|---|---|---|---|
| `regionCode` | `owner_region_code` | `region_code` | `String(64)` | Код региона (области). |
| `districtCode` | `owner_district_code` | `district_code` | `String(64)` | Код района. |
| `neighborhoodCode` | `owner_neighborhood_code` | `neighborhood_code` | `String(64)` | Код махалли/квартала. |
| `street` | `owner_street` | `street` | `String(255)` | Название улицы. |
| `houseNumber` | `owner_house_number` | `house_number` | `String(32)` | Номер дома. |
| `apartmentNumber` | `owner_apartment_number` | `apartment_number` | `String(32)` | Номер квартиры. |

### 2.4 Справочник `Form0581Status`

| Значение | Описание |
|---|---|
| `NOT_APPROVED` | Форма не утверждена (отклонена отправителем при принятии решения). |
| `SENT` | Форма отправлена получателю, ожидает принятия (`receive()`). |
| `RECEIVED` | Получатель принял форму — только после этого возможна привязка карт. |
| `CARD_LINKED` | К форме привязана как минимум одна карта. |
| `APPROVED_PENDING` | Форма ожидает окончательного решения об утверждении. |
| `APPROVED` | Форма окончательно утверждена (решение принимает организация-отправитель). |
| `CANCELED` | Форма отменена. Отмена возможна только пока форма в статусе `SENT` — после принятия получателем (`RECEIVED` и далее) отмена недоступна ни одной из сторон. |

### 2.5 Form0581OtherInjuredPerson (таблица `form058_1_other_injured_person`)

Прочие пострадавшие в том же инциденте (0..N записей), помимо основного
пациента, при `otherPeopleInjured = true`. Наследует аудиторские поля из
`UuidAuditableEntity` (см. раздел 1.1, без организационных полей).

| Поле (Java) | Колонка БД | Тип | Обязательное | Описание |
|---|---|---|---|---|
| `id`, `version`, `uuid`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy` | — | — | — | Стандартные аудиторские поля. |
| `form0581` | `form0581_id` | `Form0581` (FK) | Да | Родительская форма 058-1, к которой относится данный пострадавший. |
| `lastName` | `last_name` | `String(255)` | Нет | Фамилия пострадавшего. |
| `firstName` | `first_name` | `String(255)` | Нет | Имя пострадавшего. |
| `middleName` | `middle_name` | `String(255)` | Нет | Отчество пострадавшего. |
| `address` | `region_code`, `district_code`, `neighborhood_code`, `street`, `house_number`, `apartment_number` | `Form0581Address` (embedded) | Нет | Адрес пострадавшего (см. раздел 2.3). |
