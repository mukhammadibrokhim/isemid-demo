package uz.uzinfocom.app.modules.card.application.handler.card174;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.Card174DetailResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.card174.Card174;
import uz.uzinfocom.app.modules.card.mapper.CardCaseFieldMapperHelper;
import uz.uzinfocom.app.modules.card.mapper.CardFormMapperHelper;
import uz.uzinfocom.app.modules.card.mapper.card174.Card174MapperImpl;
import uz.uzinfocom.app.modules.card.web.dto.request.Card174Request;
import uz.uzinfocom.app.modules.card.web.dto.request.card174.AnimalMeasureRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card174.InfectionMonitoringRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card174.LaboratoryTestRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card174.OutbreakControlMeasureRequest;
import uz.uzinfocom.app.modules.card.application.exception.CardValidationException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058DateInfo;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058DiagnosisInfo;
import uz.uzinfocom.app.modules.iam.domain.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mirrors {@code Card161HandlerTest} — exercises update -> toResponse
 * through the real (generated) mapper to catch mapping bugs like a missing
 * {@code type} discriminator or a lost child back-reference. There is no
 * dedicated "create with data already filled in" handler method — cards
 * start blank and get their data via {@code update}, which is what
 * {@link #cardWith} exercises here to set up each test's starting state.
 */
class Card174HandlerTest {

    private Card174Handler handler;
    private Form058 form;

    @BeforeEach
    void setUp() {
        Card174MapperImpl mapper = new Card174MapperImpl();
        ReflectionTestUtils.setField(mapper, "cardCaseFieldMapperHelper", new CardCaseFieldMapperHelper());
        ReflectionTestUtils.setField(mapper, "cardFormMapperHelper", new CardFormMapperHelper(
                org.mockito.Mockito.mock(uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationMappingHelper.class),
                org.mockito.Mockito.mock(uz.uzinfocom.app.modules.reference.application.lookup.Icd10LookupService.class)));
        handler = new Card174Handler(mapper);

        form = mock(Form058.class);
        when(form.getId()).thenReturn(99L);
    }

    @Test
    void updateBuildsEntityGraphAndWiresBackReferences() {
        Card174Request request = requestWith("Pathogen-1",
                List.of(new InfectionMonitoringRequest(null, 1, "31234567890123", "Doe", "John", null, "M", null, null, null, null, null, LocalDate.of(2026, 7, 1), null, null, null)),
                List.of(new OutbreakControlMeasureRequest(null, 5, 1, 2, "PM1", 10, true)));

        Card174 card174 = cardWith(request);

        assertThat(card174.getForm058()).isSameAs(form);
        assertThat(card174.getCardType()).isEqualTo(CardType.CARD174);
        assertThat(card174.getPathogenType()).isEqualTo("Pathogen-1");

        assertThat(card174.getInfectionMonitoring()).hasSize(1);
        assertThat(card174.getInfectionMonitoring().getFirst().getCard174()).isSameAs(card174);

        assertThat(card174.getOutbreakControlMeasures()).hasSize(1);
        assertThat(card174.getOutbreakControlMeasures().getFirst().getCard174()).isSameAs(card174);

        assertThat(card174.getLaboratoryTests()).hasSize(2);
        assertThat(card174.getLaboratoryTests()).allSatisfy(test -> assertThat(test.getCard174()).isSameAs(card174));
    }

    @Test
    void updateReplacesChildrenInPlaceWithoutReassigningTheCollection() {
        Card174Request initial = requestWith("Pathogen-1",
                List.of(new InfectionMonitoringRequest(null, 1, "31234567890123", "Doe", "John", null, "M", null, null, null, null, null, LocalDate.of(2026, 7, 1), null, null, null)),
                List.of(new OutbreakControlMeasureRequest(null, 5, 1, 2, "PM1", 10, true)));
        Card174 card174 = cardWith(initial);
        List<?> originalList = card174.getInfectionMonitoring();

        Card174Request updated = requestWith("Pathogen-2", List.of(), List.of());
        handler.update(card174, updated);

        assertThat(card174.getPathogenType()).isEqualTo("Pathogen-2");
        assertThat(card174.getInfectionMonitoring()).isSameAs(originalList).isEmpty();
        assertThat(card174.getOutbreakControlMeasures()).isEmpty();
    }

    @Test
    void toResponseRoundTripsFieldsAndChildren() {
        Card174Request request = requestWith("Pathogen-1",
                List.of(new InfectionMonitoringRequest(null, 1, "31234567890123", "Doe", "John", null, "M", null, null, null, null, null, LocalDate.of(2026, 7, 1), null, null, null)),
                List.of(new OutbreakControlMeasureRequest(null, 5, 1, 2, "PM1", 10, true)));
        Card174 card174 = cardWith(request);

        Card174DetailResponse response = handler.toResponse(card174);

        assertThat(response.type()).isEqualTo(CardType.CARD174);
        assertThat(response.status()).isEqualTo(CardStatus.NEW);
        assertThat(response.formId()).isEqualTo(99L);
        assertThat(response.pathogenType()).isEqualTo("Pathogen-1");
        assertThat(response.lastDiseaseYear()).isEqualTo(2019);
        assertThat(response.laboratoryTests()).hasSize(2);
        assertThat(response.laboratoryTests().getFirst().sampleName()).isEqualTo("Sample1");
        assertThat(response.laboratoryTests().getFirst().sampleCount()).isEqualTo(5);
        assertThat(response.infectionMonitoring()).hasSize(1);
        assertThat(response.infectionMonitoring().getFirst().lastName()).isEqualTo("Doe");
        assertThat(response.infectionMonitoring().getFirst().pinfl()).isEqualTo("31234567890123");
        assertThat(response.infectionMonitoring().getFirst().contactInfectionDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(response.ownerPinfl()).isEqualTo("30101900000001");
        assertThat(response.animalType()).isEqualTo("AT_DOMESTIC");
        assertThat(response.animalMeasures()).hasSize(1);
        assertThat(response.animalMeasures().getFirst().measureName()).isEqualTo("Vaccination");
        assertThat(response.outbreakControlMeasures()).hasSize(1);
        assertThat(response.outbreakControlMeasures().getFirst().processingMethodCode()).isEqualTo("PM1");
    }

    @Test
    void toResponseTakesDiagnosisFromTheNotificationNotTheCard() {
        Form058DiagnosisInfo diagnosisInfo = new Form058DiagnosisInfo();
        diagnosisInfo.setIcd10Code("A22.0");
        diagnosisInfo.setIcd10Name("Anthrax");
        when(form.getDiagnosisInfo()).thenReturn(diagnosisInfo);

        Card174 card174 = cardWith(requestWith("Pathogen-1", List.of(), List.of()));
        card174.setIcd10Code("LEGACY");
        card174.setIcd10Name("Legacy name");
        card174.setHumanPrimaryDiagnosis("Legacy diagnosis");

        Card174DetailResponse response = handler.toResponse(card174);

        assertThat(response.icd10Code()).isEqualTo("A22.0");
        assertThat(response.icd10Name()).isEqualTo("Anthrax");
        assertThat(response.humanPrimaryDiagnosis()).isEqualTo("Anthrax");
    }

    @Test
    void toResponseSuggestsEmptyFieldsFromTheNotificationAndAssignedUsers() {
        Form058DateInfo dateInfo = new Form058DateInfo();
        dateInfo.setInitialReportDateTime(LocalDateTime.of(2026, 7, 17, 9, 40));
        when(form.getDateInfo()).thenReturn(dateInfo);
        User inspector = mock(User.class);
        when(inspector.getFullName()).thenReturn("Aliyev Vali");

        Card174 card174 = new Card174();
        card174.setForm058(form);
        card174.setUsers(Set.of(inspector));

        Card174DetailResponse blank = handler.toResponse(card174);

        assertThat(blank.dataObtainedDate()).isEqualTo(LocalDate.of(2026, 7, 17));
        assertThat(blank.inspectors()).isEqualTo("Aliyev Vali");
        assertThat(blank.investigationDate()).isNull();

        handler.update(card174, requestWith("Pathogen-1", List.of(), List.of()));
        Card174DetailResponse filled = handler.toResponse(card174);

        assertThat(filled.dataObtainedDate()).isEqualTo(LocalDate.now().minusDays(5));
        assertThat(filled.inspectors()).isEqualTo("Inspectors");
    }

    @Test
    void completionRequiresPathogenType() {
        Card174 card174 = cardWith(requestWith(" ", List.of(), List.of()));

        assertThatThrownBy(() -> handler.validateForCompletion(card174))
                .isInstanceOf(CardValidationException.class);

        handler.update(card174, requestWith("Pathogen-1", List.of(), List.of()));
        handler.validateForCompletion(card174);
    }

    private Card174 cardWith(Card174Request request) {
        Card174 card174 = new Card174();
        card174.setForm058(form);
        handler.update(card174, request);
        return card174;
    }

    private Card174Request requestWith(
            String pathogenType,
            List<InfectionMonitoringRequest> infectionMonitoring,
            List<OutbreakControlMeasureRequest> outbreakControlMeasures
    ) {
        return new Card174Request(
                pathogenType,
                LocalDate.now().minusDays(5), LocalDate.now().minusDays(4),
                "AnimalDx",
                LocalDate.now().minusDays(3), 2019, LocalDate.now(),
                "Localization", "Owner", "Address", "30101900000001", "AT_DOMESTIC",
                "ANIMALTYPE1", 3, "OWNERSHIP1",
                false, false, false, false, false, false, false,
                List.of("FACTOR1"),
                List.of(
                        new LaboratoryTestRequest(null, "Sample1", LocalDate.now(), 5, "Method1", "Result1"),
                        new LaboratoryTestRequest(null, "Sample2", LocalDate.now(), 2, "Method2", "Result2")),
                List.of("AFFECTED1"),
                2, 1, 1, 1, 1, 1,
                infectionMonitoring,
                "QUARANTINE1", LocalDate.now(), LocalDate.now().plusDays(10),
                "DISPOSAL1", LocalDate.now(),
                "Precaution", "Capture", "Culling",
                "DERAT1", 12.5,
                "Inspectors", "Isolation", "MeatSubmission", "Treatment", true,
                List.of("DISINFECT1"),
                3, LocalDate.now(),
                List.of("ELIM1"),
                "Location", "ExecutionResults",
                outbreakControlMeasures,
                List.of(new AnimalMeasureRequest(null, "Vaccination", LocalDate.now(), 4, "Farm")),
                "AdditionalInfo"
        );
    }
}
