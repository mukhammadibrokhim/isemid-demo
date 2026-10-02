# db/legacy-migration

Bir martalik ma'lumot ko'chirish: eski `public` schema -> yangi `public2` schema
(bir xil baza, `5434/isemid`). **Bu Liquibase changelog EMAS** — cutover'da qo'lда
ishga tushiriladi.

## To'liq qo'llanma

**[`GUIDE.md`](GUIDE.md)** — backup, sinov, tekshirish, cutover, Windows + Linux,
muammolar. Avval o'shani o'qing.

## Tez ma'lumotnoma

```
# Windows
$env:PGPASSWORD='parol'
cd db\legacy-migration
.\run.ps1 -Db isemid_test -Psql "C:\Program Files\PostgreSQL\17\bin\psql.exe" *> log.txt

# Linux
cd db/legacy-migration
PGPASSWORD=parol ./run.sh localhost 5434 isemid_test postgres 2>&1 | tee log.txt
```

Fayllar tartibi: `00-prep -> 10 -> 20 -> 30 -> 40 -> 45 -> 50 -> 51..55 -> 60 -> 61 -> 90`.
Har biri bitta tranzaksiya. `00-prep` `public2` biznes jadvallarini TRUNCATE qiladi
(qayta yugurtiriladi).

## Tamoyil

**Hech bir qator yo'qolmaydi.** NOT NULL bo'sh -> sentinel qiymat +
`public2._migration_notes` ga qayd. Yagona istisno: `act155` DETALI
(yangi sxemada bunday jadval yo'q; `act` bazaviy qatori esa ko'chadi).

## Qaror hujjatlari

- [`../../docs/legacy-migration/02-mapping-5434.md`](../../docs/legacy-migration/02-mapping-5434.md) — ustun-ustun mapping
- [`../../docs/legacy-migration/01-decisions-locked.md`](../../docs/legacy-migration/01-decisions-locked.md) — barcha qarorlar
