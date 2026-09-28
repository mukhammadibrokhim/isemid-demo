package uz.uzinfocom.app.modules.card.application.handler.card161;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.Card161DetailResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.card161.Card161;
import uz.uzinfocom.app.modules.card.domain.model.card161.Vaccination;
import uz.uzinfocom.app.modules.card.mapper.CardCaseFieldMapperHelper;
import uz.uzinfocom.app.modules.card.mapper.card161.Card161MapperImpl;
import uz.uzinfocom.app.modules.card.web.dto.request.Card161Request;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.Card161RiskFactorRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.ContactPersonRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.EmergencyProphylaxisRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.EnvironmentalLabTestRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.EnvironmentalSourceRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.HomePreventiveMeasureRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.InfectionSourceDetailRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.InfectionSourceRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.OutbreakDisinfectionMeasureRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.ScreenedGroupRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card161.VaccinationRequest;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.iam.domain.Organization;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Exercises the update -> toResponse round trip through the real
 * (generated) mapper, focused on the trickiest part of the vertical slice:
 * the "clear + repopulate" child-collection replacement that keeps
 * orphanRemoval correct without ever reassigning a Hibernate-managed
 * collection field. There is no dedicated "create with data already
 * filled in" handler method — cards start blank (see
 * {@code CardTypeHandler#createBlank()}) and get their data via
 * {@code update}, which is what {@link #cardWith} exercises here to set up
 * each test's starting state.
 */
class Card161HandlerTest {

    private Card161Handler handler;
    private EntityManager entityManager;
    private Form058 form;

    @BeforeEach
    void setUp() {
        entityManager = mock(EntityManager.class);
        Card161MapperImpl mapper = new Card161MapperImpl();
        ReflectionTestUtils.setField(mapper, "cardCaseFieldMapperHelper", new CardCaseFieldMapperHelper());
        handler = new Card161Handler(mapper, entityManager);

        form = mock(Form058.class);
        when(form.getId()).thenReturn(42L);
    }

    @Test
    void updateBuildsEntityGraphAndWiresBackReferences() {
        Organization polyclinic = new Organization();
        when(entityManager.getReference(Organization.class, 7L)).thenReturn(polyclinic);

        Card161Request request = requestWith("initial symptoms", 7L,
                List.of(new VaccinationRequest(null, "VERIFIED", "BCG", "SN-1", null, BigDecimal.valueOf(2), true, null, null, null, null, null, null, null)),
                List.of(new Card161RiskFactorRequest(null, "RF1", "Somewhere", "Summer")),
                new InfectionSourceDetailRequest("NOT_FOUND", "John Doe", "PERIOD1", "DOG"));

        Card161 card161 = cardWith(request);

        assertThat(card161.getForm058()).isSameAs(form);
        assertThat(card161.getCardType()).isEqualTo(CardType.CARD161);
        assertThat(card161.getInitialSymptoms()).isEqualTo("initial symptoms");
        assertThat(card161.getPolyclinic()).isSameAs(polyclinic);

        assertThat(card161.getVaccinations()).hasSize(1);
        assertThat(card161.getVaccinations().getFirst().getCard161()).isSameAs(card161);

        assertThat(card161.getRiskFactors()).hasSize(1);
        assertThat(card161.getRiskFactors().getFirst().getCard161()).isSameAs(card161);

        assertThat(card161.getInfectionSourceDetail()).isNotNull();
        assertThat(card161.getInfectionSourceDetail().getCard161()).isSameAs(card161);
        assertThat(card161.getInfectionSourceDetail().getPersonFullName()).isEqualTo("John Doe");
    }

    @Test
    void updateKeepsEveryFieldOfAVaccinationPrefilledFromDhpImmunization() {
        VaccinationRequest fromDhp = new VaccinationRequest(null, null, "Engerix-B", "AB123",
                LocalDateTime.of(2026, 3, 14, 10, 30), new BigDecimal("0.5"), null,
                "imm-42", "HEPB", LocalDate.of(2027, 1, 31), "ml", 2,
                List.of("Hepatitis B", " ", "Tetanus"), "Dr. Karimova");

        Card161 card161 = cardWith(requestWith("x", null, List.of(fromDhp), List.of(), null));

        Vaccination saved = card161.getVaccinations().getFirst();
        assertThat(saved.getDoseVolume()).isEqualByComparingTo("0.5");
        assertThat(saved.getFhirId()).isEqualTo("imm-42");
        assertThat(saved.getVaccineCode()).isEqualTo("HEPB");
        assertThat(saved.getExpirationDate()).isEqualTo(LocalDate.of(2027, 1, 31));
        assertThat(saved.getDoseUnit()).isEqualTo("ml");
        assertThat(saved.getDoseNumber()).isEqualTo(2);
        assertThat(saved.getTargetDiseases()).isEqualTo("Hepatitis B; Tetanus");
        assertThat(saved.getPerformerName()).isEqualTo("Dr. Karimova");

        Card161MapperImpl mapper = new Card161MapperImpl();
        assertThat(mapper.toResponse(saved).targetDiseases()).containsExactly("Hepatitis B", "Tetanus");
    }

    @Test
    void updateReplacesChildrenInPlaceWithoutReassigningTheCollection() {
        Card161Request initial = requestWith("first", null,
                List.of(new VaccinationRequest(null, "VERIFIED", "BCG", "SN-1", null, BigDecimal.valueOf(2), true, null, null, null, null, null, null, null)),
                List.of(new Card161RiskFactorRequest(null, "RF1", "Somewhere", "Summer")),
                new InfectionSourceDetailRequest("NOT_FOUND", "John Doe", "PERIOD1", "DOG"));
        Card161 card161 = cardWith(initial);
        List<?> originalVaccinationList = card161.getVaccinations();

        Card161Request updated = requestWith("second", null,
                List.of(), List.of(), null);
        handler.update(card161, updated);

        assertThat(card161.getInitialSymptoms()).isEqualTo("second");
        assertThat(card161.getVaccinations()).isSameAs(originalVaccinationList).isEmpty();
        assertThat(card161.getRiskFactors()).isEmpty();
        assertThat(card161.getInfectionSourceDetail()).isNull();
    }

    /**
     * Regression test: an earlier version always built a brand new
     * {@link Vaccination} per request, so re-saving unchanged rows deleted
     * and re-inserted them with a new auto-generated id every time. A
     * request that echoes back an existing child's id must update that same
     * managed row in place instead.
     */
    @Test
    void updatePreservesExistingChildIdsAndDropsChildrenOmittedFromTheRequest() {
        Card161Request initial = requestWith("first", null,
                List.of(
                        new VaccinationRequest(null, "V1", "BCG", "SN-1", null, BigDecimal.valueOf(1), true, null, null, null, null, null, null, null),
                        new VaccinationRequest(null, "V2", "OPV", "SN-2", null, BigDecimal.valueOf(2), false, null, null, null, null, null, null, null)
                ),
                List.of(), null);
        Card161 card161 = cardWith(initial);

        // In a real request these ids come back from the database; here we
        // assign them directly since these unit tests never actually flush.
        Vaccination firstDose = card161.getVaccinations().get(0);
        Vaccination secondDose = card161.getVaccinations().get(1);
        firstDose.setId(100L);
        secondDose.setId(200L);

        Card161Request updated = requestWith("second", null,
                List.of(
                        new VaccinationRequest(100L, "V1-EDITED", "BCG", "SN-1", null, BigDecimal.valueOf(9), true, null, null, null, null, null, null, null),
                        new VaccinationRequest(null, "V3-NEW", "MMR", "SN-3", null, BigDecimal.valueOf(3), false, null, null, null, null, null, null, null)
                ),
                List.of(), null);
        handler.update(card161, updated);

        assertThat(card161.getVaccinations()).hasSize(2);
        assertThat(card161.getVaccinations()).extracting(Vaccination::getId)
                .containsExactlyInAnyOrder(100L, null);

        Vaccination stillFirstDose = card161.getVaccinations().stream()
                .filter(v -> Long.valueOf(100L).equals(v.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(stillFirstDose).isSameAs(firstDose);
        assertThat(stillFirstDose.getVaccinationVerifiedCode()).isEqualTo("V1-EDITED");
        assertThat(stillFirstDose.getDoseVolume()).isEqualByComparingTo("9");
    }

    @Test
    void toResponseRoundTripsFieldsAndChildren() {
        Card161Request request = requestWith("initial symptoms", null,
                List.of(new VaccinationRequest(null, "VERIFIED", "BCG", "SN-1", null, BigDecimal.valueOf(2), true, null, null, null, null, null, null, null)),
                List.of(new Card161RiskFactorRequest(null, "RF1", "Somewhere", "Summer")),
                new InfectionSourceDetailRequest("NOT_FOUND", "John Doe", "PERIOD1", "DOG"));
        Card161 card161 = cardWith(request);

        Card161DetailResponse response = handler.toResponse(card161);

        assertThat(response.type()).isEqualTo(CardType.CARD161);
        assertThat(response.status()).isEqualTo(CardStatus.NEW);
        assertThat(response.formId()).isEqualTo(42L);
        assertThat(response.initialSymptoms()).isEqualTo("initial symptoms");
        assertThat(response.vaccinations()).hasSize(1);
        assertThat(response.vaccinations().getFirst().vaccinationName()).isEqualTo("BCG");
        assertThat(response.riskFactors()).hasSize(1);
        assertThat(response.infectionSourceDetail().personFullName()).isEqualTo("John Doe");
    }

    private Card161 cardWith(Card161Request request) {
        Card161 card161 = new Card161();
        card161.setForm058(form);
        handler.update(card161, request);
        return card161;
    }

    private Card161Request requestWith(
            String initialSymptoms,
            Long polyclinicId,
            List<VaccinationRequest> vaccinations,
            List<Card161RiskFactorRequest> riskFactors,
            InfectionSourceDetailRequest infectionSourceDetail
    ) {
        return new Card161Request(
                "PHONE", true, "Facility", null, "REGION1", "DISTRICT1", polyclinicId,
                initialSymptoms, "DETECTED1", null, null, null,
                "DELIVERY1", "HOMESTAY1", "LATE1", "VERIFIED1",
                vaccinations,
                LocalDate.now().minusDays(10), LocalDate.now(),
                riskFactors,
                List.<InfectionSourceRequest>of(),
                List.<EnvironmentalSourceRequest>of(),
                "LIVING1", 4, 2, "60m2",
                "WATER1", "LIQUID1", "SOLID1", "ROOM1", "YARD1", "AREA1",
                false, false, false,
                "CAUSE1", "VISITED1", "DENSE1",
                "ISOLATION1", "WATERSTATUS1", "SANITARY1", "SEWERAGE1", "FOODSTORAGE1", "FOODPREP1",
                "DISEASE_CAUSING1",
                List.<EnvironmentalLabTestRequest>of(),
                List.<ContactPersonRequest>of(),
                List.<ScreenedGroupRequest>of(),
                List.<HomePreventiveMeasureRequest>of(),
                List.<OutbreakDisinfectionMeasureRequest>of(),
                "HOSPITAL1", "INFLOC1", "PROBLOC1",
                false,
                infectionSourceDetail,
                "MAINFACTOR1",
                List.of("COND1", "COND2"),
                "OUTBREAK1", "CASESTATUS1", "EPIDEMIOLOGIST1", "ASSISTANT1",
                false,
                List.<EmergencyProphylaxisRequest>of(),
                "CLINICALFORM1",
                List.of("INJURY1"),
                "SEVERITY1",
                false,
                "SOURCEINFO1",
                "OWNERSHIP1", "OBSERVATION1", "LABTEST1"
        );
    }
}
