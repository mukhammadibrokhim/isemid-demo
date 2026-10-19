package uz.uzinfocom.app.orchestration.devseed;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import uz.uzinfocom.app.modules.act.domain.enums.ActStatus;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.act.domain.model.act153.Act153;
import uz.uzinfocom.app.modules.act.domain.model.act154.Act154;
import uz.uzinfocom.app.modules.act.domain.model.act156.Act156;
import uz.uzinfocom.app.modules.act.domain.model.act223.Act223;
import uz.uzinfocom.app.modules.act.domain.model.act224.Act224;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.domain.model.card161.Card161;
import uz.uzinfocom.app.modules.card.domain.model.card174.Card174;
import uz.uzinfocom.app.modules.card.domain.model.card175.Card175;
import uz.uzinfocom.app.modules.card.domain.model.card205.Card205;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.CardTube;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.domain.model.Form058Location;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.*;
import uz.uzinfocom.app.modules.form0581.domain.enums.Form0581Status;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.*;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129Status;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129TestOutcome;
import uz.uzinfocom.app.modules.form129.domain.enums.Form129WrightHeddelsonOutcome;
import uz.uzinfocom.app.modules.form129.domain.model.Form129;
import uz.uzinfocom.app.modules.form129.domain.model.embedded.Form129CancellationInfo;
import uz.uzinfocom.app.modules.form129.domain.model.embedded.Form129LabResults;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAddress;
import uz.uzinfocom.app.modules.patient.domain.model.PatientIdentifier;
import uz.uzinfocom.app.orchestration.devseed.DevSeedReferenceData.Icd10;
import uz.uzinfocom.app.orchestration.devseed.DevSeedReferenceData.Org;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static uz.uzinfocom.app.orchestration.devseed.DevSeedReferenceData.pick;

/**
 * Dev-profile-only bulk test data: on startup, tops each of form058,
 * form058_1, card, act and form_129 up to {@code app.dev-seed.count}
 * (default 2500) rows with realistic data spread over the last two years,
 * so lists, dashboards and reports have something to show. A table already
 * at or above that count is left alone; existing rows are never rewritten
 * (apart from flagging forms it attaches seeded cards to as
 * {@code has_linked_cards}, moving {@code ACCEPTED} ones to
 * {@code CARD_LINKED}).
 *
 * <p>Runs on a background thread after {@link ApplicationReadyEvent} so it
 * doesn't hold up startup, in small transactions. Rows go through JPA (so
 * they always match the validated entity mapping), then {@code created_at}
 * is back-dated with plain SQL, since auditing would otherwise stamp
 * "now" on everything.
 *
 * <p>Disable with {@code app.dev-seed.enabled=false}.
 */
@Slf4j
@Component
@Profile("dev")
public class DevDataSeeder {

    private static final ZoneId ZONE = ZoneId.of("Asia/Tashkent");
    private static final int BATCH_SIZE = 200;
    private static final int HISTORY_DAYS = 730;
    private static final String SOURCE = "MANUAL";

    private static final String[] MALE_FIRST_NAMES = {
            "Aziz", "Bekzod", "Jasur", "Sardor", "Otabek", "Sherzod", "Rustam", "Dilshod", "Javohir", "Ulug'bek",
            "Shohruh", "Abdulloh", "Muhammad", "Islom", "Jamshid", "Farrux", "Akmal", "Bobur", "Doniyor", "Temur"
    };
    private static final String[] FEMALE_FIRST_NAMES = {
            "Dilnoza", "Gulnora", "Madina", "Nilufar", "Sevara", "Malika", "Zarina", "Shahnoza", "Kamola", "Mohira",
            "Feruza", "Nodira", "Laylo", "Sabina", "Umida", "Dildora", "Gulbahor", "Munisa", "Hulkar", "Iroda"
    };
    private static final String[] LAST_NAME_ROOTS = {
            "Karimov", "Toshmatov", "Rahimov", "Yusupov", "Aliyev", "Ismoilov", "Qodirov", "Nazarov", "Saidov",
            "Ergashev", "Mirzayev", "Xolmatov", "Abdullayev", "Usmonov", "Normatov", "Sultonov", "Jo'rayev", "Hasanov"
    };
    private static final String[] MALE_PATRONYMICS = {
            "Akmalovich", "Botirovich", "Ravshanovich", "Shuhratovich", "Anvarovich", "Alisherovich", "Baxtiyorovich"
    };
    private static final String[] FEMALE_PATRONYMICS = {
            "Akmalovna", "Botirovna", "Ravshanovna", "Shuhratovna", "Anvarovna", "Alisherovna", "Baxtiyorovna"
    };
    private static final String[] FALLBACK_CATEGORIES = {
            "ORGANIZED", "NO_ORGANIZED", "WORKER", "NOT_EMPLOYED", "PENSIONER", "STUDENT_SCHOOL", "STUDENT",
            "MEDICAL_WORKER", "TEACHER"
    };
    private static final String[] ANIMALS = {"It", "Mushuk", "Tulki", "Bo'ri", "Kalamush", "Qoramol"};
    private static final String[] COLORS = {"Qora", "Oq", "Jigarrang", "Kulrang", "Ola"};

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final int count;
    private final boolean enabled;
    private final String schema;

    @PersistenceContext
    private EntityManager em;

    public DevDataSeeder(
            JdbcTemplate jdbc,
            PlatformTransactionManager transactionManager,
            @Value("${app.dev-seed.count:2500}") int count,
            @Value("${app.dev-seed.enabled:true}") boolean enabled,
            @Value("${spring.jpa.properties.hibernate.default_schema:}") String schema
    ) {
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactionManager);
        this.count = count;
        this.enabled = enabled;
        this.schema = schema;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (!enabled || count <= 0) {
            log.info("Dev seed disabled");
            return;
        }
        Thread thread = new Thread(this::seedSafely, "dev-data-seeder");
        thread.setDaemon(true);
        thread.start();
    }

    private void seedSafely() {
        try {
            seed();
        } catch (Exception e) {
            log.error("Dev seed failed", e);
        } finally {
            CurrentOrganizationContext.clear();
        }
    }

    private void seed() {
        int form058Missing = missing("form058");
        int form0581Missing = missing("form058_1");
        int cardMissing = missing("card");
        int actMissing = missing("act");
        int form129Missing = missing("form_129");
        if (form058Missing + form0581Missing + cardMissing + actMissing + form129Missing == 0) {
            log.info("Dev seed skipped: form058/form058_1/card/act/form_129 already hold >= {} rows", count);
            return;
        }

        DevSeedReferenceData ref = inSchema(() -> DevSeedReferenceData.load(jdbc));
        if (!ref.isUsable()) {
            log.warn("Dev seed skipped: no active organizations with region/district or no ICD-10 codes in DB "
                    + "(sync organizations / reference data first)");
            return;
        }

        Org auditOrg = ref.sesOrganizations().isEmpty() ? ref.organizations().getFirst() : ref.sesOrganizations().getFirst();
        CurrentOrganizationContext.set(Organization.builder().id(auditOrg.id()).uuid(auditOrg.uuid()).build());

        Random random = new Random();
        long started = System.currentTimeMillis();
        log.info("Dev seed started, topping up to {} rows: form058 +{}, form058_1 +{}, form_129 +{}, card +{}, act +{}",
                count, form058Missing, form0581Missing, form129Missing, cardMissing, actMissing);

        if (form058Missing > 0) {
            insertInBatches("form058", form058Missing, i -> newForm058(ref, random));
        }
        if (form0581Missing > 0) {
            insertInBatches("form058_1", form0581Missing, i -> newForm0581(ref, random));
        }
        if (form129Missing > 0) {
            insertInBatches("form_129", form129Missing, i -> newForm129(ref, random));
        }
        if (cardMissing > 0) {
            seedCards(cardMissing, random);
        }
        if (actMissing > 0) {
            seedActs(actMissing, random);
        }

        log.info("Dev seed finished in {} s", (System.currentTimeMillis() - started) / 1000);
    }

    // ------------------------------------------------------------------ infra

    /** An entity to persist plus the moment it should look like it was created. */
    private record Seeded(Object entity, Instant createdAt) {
    }

    /**
     * Runs {@code work} in a transaction whose search_path is the schema
     * Hibernate maps to ({@code hibernate.default_schema}). Without this,
     * plain SQL here follows the connection's search_path, which in dev may
     * point at the legacy {@code public} schema rather than the app's own.
     */
    private <T> T inSchema(Supplier<T> work) {
        return tx.execute(status -> {
            if (schema != null && !schema.isBlank()) {
                jdbc.execute("set local search_path to \"" + schema.replace("\"", "\"\"") + "\"");
            }
            return work.get();
        });
    }

    private void inSchema(Runnable work) {
        inSchema(() -> {
            work.run();
            return null;
        });
    }

    /** How many rows {@code table} is short of {@link #count}. */
    private int missing(String table) {
        Long existing = inSchema(() -> jdbc.queryForObject("select count(*) from " + table, Long.class));
        return (int) Math.max(0, count - (existing == null ? 0 : existing));
    }

    private void insertInBatches(String table, int total, IntFunction<Seeded> factory) {
        int done = 0;
        while (done < total) {
            int size = Math.min(BATCH_SIZE, total - done);
            int offset = done;
            inSchema(() -> {
                List<Object[]> dates = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    Seeded seeded = factory.apply(offset + i);
                    em.persist(seeded.entity());
                    Timestamp ts = Timestamp.from(seeded.createdAt());
                    dates.add(new Object[]{ts, ts, idOf(seeded.entity())});
                }
                em.flush();
                jdbc.batchUpdate("update " + table + " set created_at = ?, updated_at = ? where id = ?", dates);
                em.clear();
            });
            done += size;
            log.info("Dev seed {}: {}/{}", table, done, total);
        }
    }

    private static Long idOf(Object entity) {
        return switch (entity) {
            case Form058 f -> f.getId();
            case Form0581 f -> f.getId();
            case Form129 f -> f.getId();
            case Card c -> c.getId();
            case Act a -> a.getId();
            default -> throw new IllegalArgumentException(entity.getClass().getName());
        };
    }

    // ------------------------------------------------------------------ form058

    private Seeded newForm058(DevSeedReferenceData ref, Random random) {
        Instant created = randomPastInstant(random);
        LocalDateTime createdLocal = LocalDateTime.ofInstant(created, ZONE);
        Org sender = ref.randomOrganization(random);
        Org receiver = ref.receiverFor(sender, random);
        Icd10 icd = pick(ref.icd10Codes(), random);
        FormStatus status = weighted(random,
                FormStatus.SENT, 15, FormStatus.ACCEPTED, 10, FormStatus.CARD_LINKED, 25,
                FormStatus.APPROVED, 40, FormStatus.CANCELED, 10);

        LocalDateTime diseaseDate = createdLocal.minusDays(2 + random.nextInt(6));
        LocalDateTime firstVisit = diseaseDate.plusDays(random.nextInt(2) + 1);

        Form058 form = Form058.builder()
                .status(status)
                .source(SOURCE)
                .patient(newPatient(sender, ref, random))
                .senderOrganizationId(sender.id())
                .receiverOrganizationId(receiver.id())
                .diagnosisInfo(Form058DiagnosisInfo.builder()
                        .icd10Code(icd.code())
                        .icd10Name(icd.name())
                        .build())
                .clinicalInfo(Form058ClinicalInfo.builder()
                        .labConfirmation(random.nextBoolean())
                        .hospitalPlaceId(random.nextInt(3) == 0 ? sender.id() : null)
                        .build())
                .dateInfo(Form058DateInfo.builder()
                        .diseaseDate(diseaseDate)
                        .firstVisitDate(firstVisit)
                        .visitDate(firstVisit)
                        .diagnosisDate(firstVisit.plusHours(random.nextInt(48)))
                        .admissionDate(random.nextBoolean() ? firstVisit.plusDays(1) : null)
                        .initialReportDateTime(createdLocal)
                        .build())
                .location(random.nextInt(3) == 0 ? null : Form058Location.builder()
                        .latitude(37.2 + random.nextDouble() * 8.3)
                        .longitude(56.0 + random.nextDouble() * 17.1)
                        .location("Seed manzil")
                        .build())
                .epidemicInfo(Form058EpidemicInfo.builder()
                        .diseaseCause("Test ma'lumot")
                        .epidemicMeasures("Dezinfeksiya o'tkazildi")
                        .build())
                .reportInfo(Form058ReportInfo.builder()
                        .journalFormCode("JOURNAL_060")
                        .notifierFullName(randomFullName(random))
                        .comment("Dev seed")
                        .build())
                .hasLinkedCards(status == FormStatus.CARD_LINKED || status == FormStatus.APPROVED)
                .build();

        if (status != FormStatus.SENT && status != FormStatus.CANCELED) {
            form.setAcceptInfo(Form058AcceptInfo.builder()
                    .acceptedAt(laterThan(created, random, 1))
                    .build());
        }
        if (status == FormStatus.APPROVED) {
            Icd10 finalIcd = random.nextInt(5) == 0 ? pick(ref.icd10Codes(), random) : icd;
            form.getDiagnosisInfo().setFinalIcd10Code(finalIcd.code());
            form.getDiagnosisInfo().setFinalIcd10Name(finalIcd.name());
            form.setApprovalInfo(Form058ApprovalInfo.builder()
                    .approvedOrganizationId(sender.id())
                    .approvedOrgUuid(sender.uuid())
                    .approvedFullName(randomFullName(random))
                    .approvedAt(laterThan(created, random, 20))
                    .build());
        }
        if (status == FormStatus.CANCELED) {
            form.setCancellationInfo(Form058CancellationInfo.builder()
                    .cancelReason("Tashxis tasdiqlanmadi")
                    .canceledAt(laterThan(created, random, 3))
                    .build());
        }
        return new Seeded(form, created);
    }

    // ------------------------------------------------------------------ form058_1

    private Seeded newForm0581(DevSeedReferenceData ref, Random random) {
        Instant created = randomPastInstant(random);
        LocalDateTime createdLocal = LocalDateTime.ofInstant(created, ZONE);
        Org sender = ref.randomOrganization(random);
        Org receiver = ref.receiverFor(sender, random);
        Icd10 icd = pick(ref.animalBiteIcd10Codes(), random);
        Form0581Status status = weighted(random,
                Form0581Status.SENT, 15, Form0581Status.ACCEPTED, 10, Form0581Status.CARD_LINKED, 25,
                Form0581Status.APPROVED, 40, Form0581Status.CANCELED, 10);
        LocalDateTime injury = createdLocal.minusHours(6 + random.nextInt(72));

        Form0581 form = Form0581.builder()
                .status(status)
                .source(SOURCE)
                .patient(newPatient(sender, ref, random))
                .senderOrganizationId(sender.id())
                .receiverOrganizationId(receiver.id())
                .diagnosisInfo(Form0581DiagnosisInfo.builder()
                        .icd10Code(icd.code())
                        .icd10Name(icd.name())
                        .injuryLocalization(pick(List.of("Qo'l", "Oyoq", "Yuz", "Bosh", "Tana"), random))
                        .build())
                .incidentInfo(Form0581IncidentInfo.builder()
                        .injuryDateTime(injury)
                        .dpuVisitDateTime(injury.plusHours(1 + random.nextInt(12)))
                        .injuryRegionCode(sender.regionCode())
                        .injuryDistrictCode(sender.districtCode())
                        .injuryAddress("Seed ko'chasi, " + (1 + random.nextInt(120)))
                        .build())
                .animalInfo(Form0581AnimalInfo.builder()
                        .animalType(pick(List.of(ANIMALS), random))
                        .animalColor(pick(List.of(COLORS), random))
                        .build())
                .animalOwnerInfo(random.nextBoolean() ? null : Form0581AnimalOwnerInfo.builder()
                        .ownerLastName(pick(List.of(LAST_NAME_ROOTS), random))
                        .ownerFirstName(pick(List.of(MALE_FIRST_NAMES), random))
                        .ownerAddress(Form0581Address.builder()
                                .regionCode(sender.regionCode())
                                .districtCode(sender.districtCode())
                                .street("Seed ko'chasi")
                                .houseNumber(String.valueOf(1 + random.nextInt(120)))
                                .build())
                        .build())
                .otherPeopleInjured(false)
                .hospitalizationInfo(random.nextInt(4) == 0
                        ? Form0581HospitalizationInfo.builder()
                        .hospitalizedAt(injury.plusHours(3))
                        .hospitalOrganizationId(sender.id())
                        .build()
                        : null)
                .reportInfo(Form0581ReportInfo.builder()
                        .antirabicAssistanceInfo("KOKAV bilan emlash boshlandi")
                        .notifierFullName(randomFullName(random))
                        .messageSentAt(createdLocal)
                        .build())
                .hasLinkedCards(status == Form0581Status.CARD_LINKED || status == Form0581Status.APPROVED)
                .build();

        if (status != Form0581Status.SENT && status != Form0581Status.CANCELED) {
            form.getReportInfo().setReceiverFullName(randomFullName(random));
            form.setAcceptInfo(Form0581AcceptInfo.builder().acceptedAt(laterThan(created, random, 1)).build());
        }
        if (status == Form0581Status.APPROVED) {
            form.getDiagnosisInfo().setFinalIcd10Code(icd.code());
            form.getDiagnosisInfo().setFinalIcd10Name(icd.name());
            form.setApprovalInfo(Form0581ApprovalInfo.builder()
                    .approvedOrganizationId(sender.id())
                    .approvedOrgUuid(sender.uuid())
                    .approvedFullName(randomFullName(random))
                    .approvedAt(laterThan(created, random, 20))
                    .build());
        }
        if (status == Form0581Status.CANCELED) {
            form.setCancellationInfo(Form0581CancellationInfo.builder()
                    .cancelReason("Noto'g'ri yuborilgan")
                    .canceledAt(laterThan(created, random, 3))
                    .build());
        }
        return new Seeded(form, created);
    }

    // ------------------------------------------------------------------ form129

    private Seeded newForm129(DevSeedReferenceData ref, Random random) {
        Instant created = randomPastInstant(random);
        Org sender = ref.randomOrganization(random);
        Org receiver = ref.receiverFor(sender, random);
        Form129Status status = weighted(random,
                Form129Status.SENT, 30, Form129Status.ACCEPTED, 60, Form129Status.CANCELED, 10);

        Form129LabResults labResults = switch (random.nextInt(3)) {
            case 0 -> Form129LabResults.builder()       // syphilis
                    .rwOutcome(outcome(random))
                    .rprVdrlOutcome(outcome(random))
                    .tphaOutcome(outcome(random))
                    .elisaOutcome(outcome(random))
                    .build();
            case 1 -> Form129LabResults.builder()       // hepatitis B
                    .hbsAgOutcome(outcome(random))
                    .antiHbcIgGOutcome(outcome(random))
                    .antiHbcIgMOutcome(outcome(random))
                    .pcrQualitativeOutcome(outcome(random))
                    .build();
            default -> Form129LabResults.builder()      // brucellosis
                    .wrightHeddelsonOutcome(Form129WrightHeddelsonOutcome.values()[random.nextInt(3)])
                    .wrightHeddelsonResultText("1:" + (50 << random.nextInt(4)))
                    .build();
        };

        Form129 form = Form129.builder()
                .status(status)
                .source(SOURCE)
                .reportingInstitutionName(sender.name())
                .medicalId("MED-" + (100000 + random.nextInt(900000)))
                .patient(newPatient(sender, ref, random))
                .senderOrganizationId(sender.id())
                .receiverOrganizationId(receiver.id())
                .labResults(labResults)
                .notifierFullName(randomFullName(random))
                .receiverFullName(status == Form129Status.ACCEPTED ? randomFullName(random) : null)
                .build();
        if (status == Form129Status.CANCELED) {
            form.setCancellationInfo(new Form129CancellationInfo("Takroriy xabarnoma", null, laterThan(created, random, 3)));
        }
        return new Seeded(form, created);
    }

    private static Form129TestOutcome outcome(Random random) {
        return random.nextInt(3) == 0 ? Form129TestOutcome.POSITIVE : Form129TestOutcome.NEGATIVE;
    }

    // ------------------------------------------------------------------ cards

    private record ParentCase(long id, boolean form0581, Instant createdAt) {
    }

    /**
     * Cards hang off forms the receiver already accepted ({@code CARD_LINKED}
     * / {@code APPROVED}, any existing ones included, not only seeded ones);
     * the forms they land on get {@code has_linked_cards} flipped on.
     */
    private void seedCards(int total, Random random) {
        List<ParentCase> parents = new ArrayList<>(inSchema(() -> jdbc.query("""
                        select id, false as f0581, created_at from form058
                        where deleted = false and status in ('ACCEPTED', 'CARD_LINKED', 'APPROVED')
                        union all
                        select id, true as f0581, created_at from form058_1
                        where deleted = false and status in ('ACCEPTED', 'CARD_LINKED', 'APPROVED')
                        """,
                (rs, i) -> new ParentCase(rs.getLong("id"), rs.getBoolean("f0581"),
                        rs.getTimestamp("created_at").toInstant()))));
        if (parents.isEmpty()) {
            log.warn("Dev seed: no accepted form058/form058_1 to attach cards to — cards skipped");
            return;
        }

        insertInBatches("card", total, i -> {
            ParentCase parent = pick(parents, random);
            Card card = parent.form0581()
                    ? new Card205()
                    : switch (random.nextInt(4)) {
                        case 0 -> new Card161();
                        case 1 -> new Card174();
                        case 2 -> new Card175();
                        default -> new CardTube();
                    };
            if (parent.form0581()) {
                card.setForm0581(em.getReference(Form0581.class, parent.id()));
            } else {
                card.setForm058(em.getReference(Form058.class, parent.id()));
            }
            CardStatus status = weighted(random,
                    CardStatus.NEW, 15, CardStatus.ACCEPTED_BY_USER, 10, CardStatus.IN_PROGRESS, 20,
                    CardStatus.COMPLETED, 15, CardStatus.APPROVED, 30, CardStatus.REJECTED_BY_USER, 5,
                    CardStatus.REJECTED, 5);
            card.setStatus(status);
            Instant created = laterThan(parent.createdAt(), random, 3);
            if (status == CardStatus.COMPLETED || status == CardStatus.APPROVED) {
                card.setCompletedDate(LocalDate.ofInstant(laterThan(created, random, 10), ZONE));
            }
            if (status == CardStatus.REJECTED) {
                card.setSupervisorComment("Qayta ko'rib chiqing");
            }
            return new Seeded(card, created);
        });

        inSchema(() -> {
            jdbc.update("""
                    update form058 set has_linked_cards = true,
                        status = case when status = 'ACCEPTED' then 'CARD_LINKED' else status end
                    where id in (select form058_id from card where form058_id is not null)
                    """);
            jdbc.update("""
                    update form058_1 set has_linked_cards = true,
                        status = case when status = 'ACCEPTED' then 'CARD_LINKED' else status end
                    where id in (select form058_1_id from card where form058_1_id is not null)
                    """);
        });
    }

    // ------------------------------------------------------------------ acts

    private record ParentCard(long id, Instant createdAt) {
    }

    private void seedActs(int total, Random random) {
        List<ParentCard> cards = inSchema(() -> jdbc.query("""
                        select id, created_at from card
                        where deleted = false and status not in ('NEW', 'REJECTED_BY_USER')
                        """,
                (rs, i) -> new ParentCard(rs.getLong("id"), rs.getTimestamp("created_at").toInstant())));
        if (cards.isEmpty()) {
            log.warn("Dev seed: no in-work cards to attach acts to — acts skipped");
            return;
        }

        insertInBatches("act", total, i -> {
            ParentCard parent = pick(cards, random);
            ActType type = ActType.values()[random.nextInt(ActType.values().length)];
            Act act = switch (type) {
                case ACT153 -> new Act153();
                case ACT154 -> new Act154();
                case ACT156 -> new Act156();
                case ACT223 -> new Act223();
                case ACT224 -> new Act224();
            };
            act.setActType(type);
            act.setActStatus(weighted(random,
                    ActStatus.NEW, 20, ActStatus.IN_PROGRESS, 20, ActStatus.READY, 10, ActStatus.SENT, 15,
                    ActStatus.SEND_FAILED, 5, ActStatus.RETURNED_BY_LIS, 5, ActStatus.RESULT_RECEIVED, 10,
                    ActStatus.COMPLETED, 15));
            act.setCard(em.getReference(Card.class, parent.id()));
            act.setResultComment(act.getActStatus() == ActStatus.COMPLETED ? "Natija me'yorda" : null);
            return new Seeded(act, laterThan(parent.createdAt(), random, 5));
        });

        // act_number mirrors the id, as the create flow does
        inSchema(() -> {
            jdbc.update("update act set act_number = id where act_number is null");
        });
    }

    // ------------------------------------------------------------------ patient

    private Patient newPatient(Org org, DevSeedReferenceData ref, Random random) {
        boolean male = random.nextBoolean();
        LocalDate birth = LocalDate.now(ZONE)
                .minusDays(random.nextInt(365))
                .minusYears(ageYears(random));
        Period age = Period.between(birth, LocalDate.now(ZONE));
        String root = pick(List.of(LAST_NAME_ROOTS), random);

        Patient patient = Patient.builder()
                .firstName(pick(List.of(male ? MALE_FIRST_NAMES : FEMALE_FIRST_NAMES), random))
                .lastName(male ? root : root + "a")
                .middleName(pick(List.of(male ? MALE_PATRONYMICS : FEMALE_PATRONYMICS), random))
                .birthDate(birth)
                .ageYears(age.getYears())
                .ageMonths(age.getMonths())
                .genderCode(male ? "MALE" : "FEMALE")
                .phoneNumber("+99890" + (1000000 + random.nextInt(9000000)))
                .populationTypeCode(random.nextInt(5) < 2 ? "VILLAGE_RESIDENT" : "CITY_RESIDENT")
                .categoryCode(ref.categoryCodes().isEmpty()
                        ? pick(List.of(FALLBACK_CATEGORIES), random)
                        : pick(ref.categoryCodes(), random))
                .build();

        patient.addIdentifier(PatientIdentifier.builder()
                .typeCode("NNUZB")
                .value(String.valueOf(30000000000000L + (long) (random.nextDouble() * 9_999_999_999_999L)))
                .build());
        patient.addAddress(PatientAddress.builder()
                .type(AddressType.PERMANENT)
                .regionCode(org.regionCode())
                .districtCode(org.districtCode())
                .streetAddress("Seed ko'chasi")
                .houseNumber(String.valueOf(1 + random.nextInt(150)))
                .build());
        // Patient isn't cascaded from the case forms — it has its own lifecycle
        em.persist(patient);
        return patient;
    }

    /** Skewed towards children/young adults so the under-1/under-14/under-18 report cuts aren't empty. */
    private static int ageYears(Random random) {
        int bucket = random.nextInt(10);
        if (bucket == 0) {
            return 0;
        }
        if (bucket <= 3) {
            return 1 + random.nextInt(14);
        }
        if (bucket == 4) {
            return 14 + random.nextInt(4);
        }
        return 18 + random.nextInt(62);
    }

    // ------------------------------------------------------------------ helpers

    private static Instant randomPastInstant(Random random) {
        long seconds = (long) (random.nextDouble() * Duration.ofDays(HISTORY_DAYS).toSeconds());
        return Instant.now().minusSeconds(seconds);
    }

    /** A moment up to {@code maxDays} after {@code from}, never in the future. */
    private static Instant laterThan(Instant from, Random random, int maxDays) {
        Instant candidate = from.plusSeconds((long) (random.nextDouble() * Duration.ofDays(maxDays).toSeconds()));
        Instant now = Instant.now();
        return candidate.isAfter(now) ? now : candidate;
    }

    private static String randomFullName(Random random) {
        return pick(List.of(LAST_NAME_ROOTS), random) + " " + pick(List.of(MALE_FIRST_NAMES), random);
    }

    /** Picks from {@code value, weight, value, weight, ...} pairs. */
    @SuppressWarnings("unchecked")
    private static <T> T weighted(Random random, Object... valueWeightPairs) {
        int total = 0;
        for (int i = 1; i < valueWeightPairs.length; i += 2) {
            total += (Integer) valueWeightPairs[i];
        }
        int roll = random.nextInt(total);
        for (int i = 0; i < valueWeightPairs.length; i += 2) {
            roll -= (Integer) valueWeightPairs[i + 1];
            if (roll < 0) {
                return (T) valueWeightPairs[i];
            }
        }
        return (T) valueWeightPairs[0];
    }
}
