package uz.uzinfocom.app.modules.card.application.handler.card174;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.Card174DetailResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.card174.Card174;
import uz.uzinfocom.app.modules.card.mapper.card174.Card174MapperImpl;
import uz.uzinfocom.app.modules.card.web.dto.request.Card174Request;
import uz.uzinfocom.app.modules.card.web.dto.request.card174.InfectionMonitoringRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card174.OutbreakControlMeasureRequest;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mirrors {@code Card161HandlerTest} — exercises create -> update ->
 * toResponse through the real (generated) mapper to catch mapping bugs like
 * a missing {@code type} discriminator or a lost child back-reference.
 */
class Card174HandlerTest {

    private Card174Handler handler;
    private Form058 form;

    @BeforeEach
    void setUp() {
        handler = new Card174Handler(new Card174MapperImpl());

        form = mock(Form058.class);
        when(form.getId()).thenReturn(99L);
    }

    @Test
    void createBuildsEntityGraphAndWiresBackReferences() {
        Card174Request request = requestWith("MKB-1",
                List.of(new InfectionMonitoringRequest(1, "Doe", "John", null, "M", null, null, null, null, null, null, null, null)),
                List.of(new OutbreakControlMeasureRequest(5, 1, 2, "PM1", 10, true)));

        Card174 card174 = handler.create(form, request);

        assertThat(card174.getForm058()).isSameAs(form);
        assertThat(card174.getCardType()).isEqualTo(CardType.CARD174);
        assertThat(card174.getMkb10Code()).isEqualTo("MKB-1");

        assertThat(card174.getInfectionMonitoring()).hasSize(1);
        assertThat(card174.getInfectionMonitoring().getFirst().getCard174()).isSameAs(card174);

        assertThat(card174.getOutbreakControlMeasures()).hasSize(1);
        assertThat(card174.getOutbreakControlMeasures().getFirst().getCard174()).isSameAs(card174);
    }

    @Test
    void updateReplacesChildrenInPlaceWithoutReassigningTheCollection() {
        Card174Request initial = requestWith("MKB-1",
                List.of(new InfectionMonitoringRequest(1, "Doe", "John", null, "M", null, null, null, null, null, null, null, null)),
                List.of(new OutbreakControlMeasureRequest(5, 1, 2, "PM1", 10, true)));
        Card174 card174 = handler.create(form, initial);
        List<?> originalList = card174.getInfectionMonitoring();

        Card174Request updated = requestWith("MKB-2", List.of(), List.of());
        handler.update(card174, updated);

        assertThat(card174.getMkb10Code()).isEqualTo("MKB-2");
        assertThat(card174.getInfectionMonitoring()).isSameAs(originalList).isEmpty();
        assertThat(card174.getOutbreakControlMeasures()).isEmpty();
    }

    @Test
    void toResponseRoundTripsFieldsAndChildren() {
        Card174Request request = requestWith("MKB-1",
                List.of(new InfectionMonitoringRequest(1, "Doe", "John", null, "M", null, null, null, null, null, null, null, null)),
                List.of(new OutbreakControlMeasureRequest(5, 1, 2, "PM1", 10, true)));
        Card174 card174 = handler.create(form, request);

        Card174DetailResponse response = handler.toResponse(card174);

        assertThat(response.type()).isEqualTo(CardType.CARD174);
        assertThat(response.status()).isEqualTo(CardStatus.NEW);
        assertThat(response.formId()).isEqualTo(99L);
        assertThat(response.mkb10Code()).isEqualTo("MKB-1");
        assertThat(response.infectionMonitoring()).hasSize(1);
        assertThat(response.infectionMonitoring().getFirst().lastName()).isEqualTo("Doe");
        assertThat(response.outbreakControlMeasures()).hasSize(1);
        assertThat(response.outbreakControlMeasures().getFirst().processingMethodCode()).isEqualTo("PM1");
    }

    private Card174Request requestWith(
            String mkb10Code,
            List<InfectionMonitoringRequest> infectionMonitoring,
            List<OutbreakControlMeasureRequest> outbreakControlMeasures
    ) {
        return new Card174Request(
                1, mkb10Code, "Name", "PathogenType",
                LocalDate.now().minusDays(5), LocalDate.now().minusDays(4),
                "AnimalDx", "HumanDx",
                LocalDate.now().minusDays(3), LocalDate.now().minusYears(1), LocalDate.now(),
                "Localization", "Owner", "Address",
                "ANIMALTYPE1", 3, "OWNERSHIP1",
                false, false, false, false, false, false, false,
                List.of("FACTOR1"),
                "AnimalType1", LocalDate.now(), 5, "Method1", "Result1",
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
                "AdditionalInfo"
        );
    }
}
