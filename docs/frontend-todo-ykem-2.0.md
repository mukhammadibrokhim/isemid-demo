# YKEM 2.0 test hujjati — frontend uchun to'liq vazifalar ro'yxati

Manba: `YKEM 2.0 Test (1).docx` (17 ta band + 13.11.2026 yozuvlari).
Frontend repo: `isemid-demo-front/frontend`, fayl yo'llari `src/` ga nisbatan.

Belgilar:
- **[FRONT]** — faqat frontend ishi.
- **[FRONT+BACK]** — backend tayyor, frontend moslashishi kerak.
- **[MA'LUMOT]** — bazadagi ma'lumotga bog'liq, kodda sabab topilmadi.
- **[TAYYOR]** — backendda allaqachon tuzatilgan, frontendda qayta tekshirish kifoya.

---

## A. Backend o'zgarishlari tufayli frontend moslashishi (birinchi navbatda)

### A1. Form 129 — F.I.Sh. avtomatik [FRONT+BACK]
Hujjat (13.11): «kimmi ro'lidan kirib to'ldirsa shuning familiyasi chiqadi», «SES qabul qiladi, kimmi ro'lidan kirsa shu avtomatik ko'rinadi, tanlash shart emas».

Backend: `notifierFullName` (yaratishda) va `receiverFullName` (qabul qilishda) bo'sh kelsa, joriy foydalanuvchining F.I.Sh. ini o'zi yozadi. Yaratishdagi `notifierFullName` endi majburiy emas.

Frontend:
1. `entities/medical-form/types/form129.ts` — `notifierFullName` majburiy emas (izohdagi «is required» ni yangilang).
2. 129 yaratish formasi — «Xabar bergan shaxs F.I.Sh.» maydonini yashiring yoki faqat o'qish uchun qiling; bo'sh yuboring.
3. «Xabarnomani qabul qilish» dialogi — «Xabarnomani qabul qilgan (F.I.Sh.)» maydonini olib tashlang yoki ixtiyoriy qiling; bo'sh yuboring (`receiverFullName` yuborilmasin).
4. 129 detail sahifasi («Xabarnoma» bloki) — «Xabarnomani qabul qilgan» endi «—» emas, backend qiymatini ko'rsating.

### A2. Tasdiqlangan (APPROVED) xabarnomaga karta biriktirish [FRONT+BACK]
Hujjat #13: ЭИ №265 `APPROVED`, «Прикрепить карты» → xato «Обновление Form058 не разрешено для статуса: APPROVED».

Backend: `APPROVED` holatda ham karta biriktiriladi; holat `APPROVED` qoladi (058 va 058-1).

Frontend:
1. «Прикрепить карты» tugmasi `APPROVED` da ham ko'rinsin va ishlasin (yashirilgan bo'lsa ochish).
2. «Karta biriktirilgan» belgisi/filtri faqat `status === 'CARD_LINKED'` ga bog'lanmasin: `hasLinkedCards` ni ham hisobga oling.
3. Biriktirgandan keyin ro'yxat va detailni qayta yuklang.

### A3. Dublikat ЭИ ni o'chirish [FRONT+BACK]
Hujjat #10: kasalxona bir bemorni bir necha marta yuboradi, SES qabul qilib qo'ygan; o'chirib bo'lmaydi («Удаление Form058 не разрешено для статуса: ACCEPTED»).

Backend: `SENT` va `ACCEPTED` o'chiriladi (soft delete, sabab majburiy). `CARD_LINKED`, `APPROVED`, `CANCELED` qulflangan. Xuddi shu qoida 058-1 uchun ham.

Frontend:
1. «Удалить» tugmasi `SENT` va `ACCEPTED` da faol; `CARD_LINKED`/`APPROVED` da yashirin yoki disabled (tooltip sababi bilan).
2. Xatolik matni: backend xabarini ko'rsating (xom `ACCEPTED` enum'ni emas, status nomini lokalizatsiya qiling).

### A4. Hisobotlar — raqamlar o'zgaradi [FRONT: faqat tekshirish]
1. **Ayollar** (Form 1, 7, 28.1, Statistika) endi hisoblanadi (oldin 0 edi) — #15, #17.
2. **«Birlamchi»** (Form 1, Form 4): endi bekor qilinmagan hamma ЭИ (tasdiqlanganlari ham). 3 ta gepatit A uchun 3 chiqadi — #16.
3. **Tuman** (viloyat/tuman/respublika doirasidagi SES foydalanuvchilari): ЭИ **qabul qiluvchi** SES tumaniga yoziladi (Sergeli SES ga yuborilgan → Sergeli) — #15. Kasalxona foydalanuvchisi o'z tashkiloti bo'yicha ko'radi (yuboruvchi).
4. **Form 28.1**: «Острый гепатит А» (B15.0–B15.9) endi B15.9 ni ham sanaydi — #17.
5. Hisobot sarlavhalarida/izohlarida «birlamchi = tasdiqlanmagan» deb yozilgan bo'lsa, matnni yangilang (Form 1 va 4 uchun).

---

## B. Katalog (dropdown) nomlari [FRONT+BACK] — #8, #9 va epid.karta

Muammo: front `entities/reference/types/catalog.ts` dagi `CATALOG_TYPES` da bazadagi legacy nomlardan farqli nomlarni so'raydi; noma'lum type uchun API `200 []` qaytaradi → selectda «Ничего не найдено».

Backend 19 ta nomni asl nomga yo'naltiradi (shuning uchun hozir ham ishlaydi), lekin toza yechim — frontda nomlarni almashtirish (`useCatalogOptions(...)` / `useLabelOf(...)` chaqiriqlarini ham: Card161Sections, Card161Source, Card161Document, Card161DocumentParts, Card175Sections, Card175Document, Card174/205 hujjatlari).

| Frontend hozir | Bazadagi (to'g'ri) nom | Qayerda |
|---|---|---|
| `DETECTED` | `DEFINED_DISEASE` | «Способ выявления» |
| `INFECTION_SOURCE` | `NOT_FOUND_INFECTION` | «Причина, по которой источник не найден» |
| `LATE_ADMISSION_REASON` | `REASON_OF_LATE_HOSPITALIZATION` | «Причина позднего обращения» |
| `HOME_STAY_REASON` | `REASON_STAY_HOME` | «Причина исключения домашней изоляции» |
| `HOSPITAL_DISCHARGE` | `HOSPITAL_DISCHARGE_STATUS` | |
| `AREA_CONDITION` | `AREA_CONTIDION` | «Состояние прилегающей местности» (nom xato bilan shunday saqlangan) |
| `CONDUCTED_LOCATION` | `COUNDUCTED_LOCA` | «Место проведения (по справочнику)» |
| `INFECTION_CONDITION` | `INFE_CONDITION` | «Условия, способствовавшие заражению» |
| `INFECTION_LOCATION` | `INFECTED_LOCATION` | «Место заражения» |
| `LIQ_WASTE_TYPE` | `LIQUID_WASTE` | «Утилизация жидких отходов» |
| `SOLID_WASTE_TYPE` | `SOLID_WASTE` | «Утилизация твёрдых отходов» |
| `MAIN_PROBABLE_INFECTION_FACTOR` | `MAIN_FACTOR` | «Основной предполагаемый фактор» |
| `OBSERVATION_RESULT_ANIMALS` | `OBSERVATION_RESULT_OF_ANIMALS` | «Результат наблюдения за животным» |
| `PATHOGEN_FACTOR` | `PATHOGEN_MAIN_FACTOR` | |
| `PATIENT_COME` | `WHERE_PATIENT_COME` | Card175 |
| `PREVENTION_AND_AID` | `INFORMATION_ABOUT_IMMEDIATE_PREVENTION_AND_ANTIRABIC_AID` | Card175 |
| `PROBABLE_INFECTION_LOCATION` | `PROBABLE_INFECTION_LOC` | «Предполагаемое место заражения» |
| `RELEVANCE_OF_DISEASE` | `RELEVANCE_OF_DISEASE_TO_PROFESSION` | |
| `VISIT_OBJECTS` | `VISITET_OBJ` | «Посещённые объекты» |

Hali ham bo'sh bo'ladigan (bazada mos katalog YO'Q) — qiymatlarini biznes berishi kerak, keyin backendda seed qilinadi:
- `DELIVERY_METHOD` — «Способ доставки» (Card161).
- `ANIMAL_LAB_TEST_RESULT` — «Результат лабораторного исследования животного».

Shu paytgacha: bo'sh selectda tushunarli bo'sh holat matni yoki oddiy matn maydoni.

Tekshirish: Network'da `/v1/references/catalogs/types/<NOM>` javobi `[]` bo'lgan hamma nomni yig'ib yuboring; ro'yxatdan tashqari nom chiqsa, shu jadvalga qo'shamiz.

---

## C. Hujjatning bandlari

| # | Hujjat bandi | Nima qilish kerak | Turi |
|---|---|---|---|
| 1 | Yuklamalarda `form1_export_5.xlsx` ostida `FORM1` | Subtitr xom export type bo'lib chiqyapti. Export type → lokalizatsiyalangan nom jadvali: `FORM1` → «Форма №1», `FORM4` → «Форма №4» va h.k., hamma hisobot type'lari uchun. | FRONT |
| 1 | Excel «Код» ustunida `UZ-AN`, `AN-401`, `25`, `91`… aralash | Backend export. Tashkilot kodlari shunday saqlangan; kodni yashirish yoki nom bilan almashtirish bo'yicha alohida qaror kerak. | Qaror |
| 2 | Jurnal ф-060 sarlavhasi eski buyruq (№363) | Backendda allaqachon №399 / 26.12.2025. Yangi build'da tekshiring. | TAYYOR |
| 3 | Sergeli: 20 ta mahalla, 46 bo'lishi kerak | Backend default 200; front `BY_PARENT_LIMIT = 200`. Yangi build'da tekshiring. | TAYYOR |
| 4 | Diagnoz qidirishda modal oyna pastga tushadi | Modal/popover pozitsiyasi (portal, z-index, `max-height`, flip). | FRONT |
| 5 | ЭИ da «xaritadan qidirish» ishlamaydi | Xarita tanlash komponentini tuzatish. | FRONT |
| 6 | Qabul qilingandan keyin elementlar takrorlanadi; muddati o'tgan hujjat ko'rinadi | Takrorlanish backendda tuzatilgan (11-noyabr). Muddati o'tgan hujjatni (`Срок действия истёк`) yaratish formasida yashiring yoki ajratib belgilang. | TAYYOR + FRONT |
| 7 | Karta: ish joyi nomi «—» | Backendda tuzatilgan. Qayta tekshiring. | TAYYOR |
| 8, 9 | Epid.kartada dropdownlar bo'sh | B bo'limi. | FRONT+BACK |
| 10 | Dublikat ЭИ ni o'chirish | A3. | FRONT+BACK |
| 11 | Karta 205 (058-1): bemor ma'lumotlari oldindan to'ldirilmaydi; «Обращение в ЛПУ» bo'sh | Karta 205 ni ochganda bemor ma'lumotlarini ЭИ dan to'ldirish (Card161 dagidek). «Дата обращения в ЛПУ» va «Лечебно-профилактическое учреждение»: 058-1 da bor-yo'qligini tekshiring; yo'q bo'lsa backendga maydon qo'shish kerak. | FRONT (+BACK?) |
| 12 | «Порода», «Окрас» bo'sh | Card205 «Животное и владелец»: 058-1 dan mos maydonni oldindan to'ldirish; backend javobida maydon bormi tekshiring. | FRONT (+BACK?) |
| 13 | Tasdiqlangan ЭИ, karta ochilmagan | A2. | FRONT+BACK |
| 14–17 | Hisobotlar | A4. | FRONT (tekshirish) |

---

## D. 13.11.2026 yozuvlari (129 va boshqa shakllar)

| Yozuv | Nima qilish kerak | Turi |
|---|---|---|
| Kasb tanlashda faqat «A» harfidagilar chiqadi | `PROFESSION` (~12 000 yozuv): `SELECT_PAGE_SIZE = 20` (`entities/reference/api/reference.api.ts`) — ochilganda faqat alfavit bo'yicha birinchi 20 ta. Qidiruv `name` parametri bilan ketayotganini tekshiring (debounce, bo'sh matn). Limitni 100–200 ga ko'taring (backend maksimumi 200). | FRONT |
| Hududiy SES topilmayapti (129, Mirzo Ulug'bek) | «Tanlangan hudud bo'yicha tashkilotlar topilmadi». Viloyat/tuman filtri va faqat SES turi to'g'ri yuborilayotganini tekshiring. Dev bazada shu tumanda SES bormi — tekshirish kerak. | FRONT + MA'LUMOT |
| Sergeli mahallalari to'liq emas («Xonobod») | Bazada `Xonobod` (TK-283023) bor. Limit 200; yangi build'da qayta tekshiring. | TAYYOR / MA'LUMOT |
| Kimmi ro'lidan kirsa familiyasi chiqsin | A1. | FRONT+BACK |
| Qabul qilingan shakllarda «Yuborilgan» emas, «Yangi»/«Kelib tushgan» | `entities/medical-form/lib/form129-enums.ts` (va 058, 058-1 «Полученные формы»): qabul qiluvchi tomondagi ro'yxatda `SENT` yorlig'i «Yangi» yoki «Kelib tushgan»; yuboruvchida «Yuborilgan». 5 til uchun tarjima. | FRONT |
| SES qabul qiladi, FIO avtomatik, tanlash shart emas | A1 (qabul qilish dialogi). | FRONT+BACK |

---

## E. Backend tomonda qolgan

- `DELIVERY_METHOD` va `ANIMAL_LAB_TEST_RESULT` katalog qiymatlari (biznes beradi, keyin seed).
- Hisobotlardagi «Kod» ustuni bo'yicha qaror.
- Form 2, 6, 9, Statistika da «birlamchi» ta'rifi eski (`APPROVED/CANCELED` emas).
- Qabul qiluvchi bo'yicha tuman hisobi dev bazada hali sinalmagan: Sergeli SES foydalanuvchisi bilan Form 1 ni oching, gepatit A va ayollarni tekshiring.

## F. Tavsiya etilgan tartib

1. A1, A2, A3 (backend tayyor, tez natija).
2. B (katalog nomlari) + bo'sh holatlar.
3. D: kasb limiti, status yorlig'i.
4. C: 1, 4, 5, 6, 11, 12.
5. A4 — testerga raqamlar o'zgarganini aytib, hisobotlarni qayta tekshirtiring.
