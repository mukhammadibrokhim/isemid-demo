package uz.uzinfocom.app.modules.card.application.handler.card_tube;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.CardTubeDetailResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.card_tube.CardTube;
import uz.uzinfocom.app.modules.card.mapper.card_tube.CardTubeMapperImpl;
import uz.uzinfocom.app.modules.card.web.dto.request.CardTubeRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card_tube.ContactMonitoringRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card_tube.InfectionSourceRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card_tube.TBHistoryRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card_tube.XRayRequest;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * There is no dedicated "create with data already filled in" handler
 * method — cards start blank and get their data via {@code update}, which
 * is what {@link #cardWith} exercises here to set up each test's starting
 * state.
 */
class CardTubeHandlerTest {

    private CardTubeHandler handler;
    private Form058 form;

    @BeforeEach
    void setUp() {
        handler = new CardTubeHandler(new CardTubeMapperImpl());

        form = mock(Form058.class);
        when(form.getId()).thenReturn(21L);
    }

    @Test
    void updateBuildsEntityGraphAndWiresBackReferences() {
        CardTubeRequest request = requestWith("DISP-1",
                List.of(new XRayRequest(null, LocalDate.now(), "Clinic1", "Clear")),
                List.of(new TBHistoryRequest(null, "Loc1", LocalDate.now(), "MKB1", "Name1", "Group1")),
                List.of(new InfectionSourceRequest(null, "TB1", "John Doe", "REL1", "6 months")),
                List.of(new ContactMonitoringRequest(null, "Jane Doe", LocalDate.now(), 30, "REL1", "Workplace1", "Receiver1", LocalDate.now(), "STATUS1")));

        CardTube cardTube = cardWith(request);

        assertThat(cardTube.getForm058()).isSameAs(form);
        assertThat(cardTube.getCardType()).isEqualTo(CardType.CARD_TUBE);
        assertThat(cardTube.getDispensaryId()).isEqualTo("DISP-1");

        assertThat(cardTube.getPreMBTChestXRay()).hasSize(1);
        assertThat(cardTube.getPreMBTChestXRay().getFirst().getCardTube()).isSameAs(cardTube);

        assertThat(cardTube.getPreviousTBHistory()).hasSize(1);
        assertThat(cardTube.getPreviousTBHistory().getFirst().getCardTube()).isSameAs(cardTube);

        assertThat(cardTube.getPossibleInfectionSources()).hasSize(1);
        assertThat(cardTube.getPossibleInfectionSources().getFirst().getCardTube()).isSameAs(cardTube);

        assertThat(cardTube.getContactMonitoringList()).hasSize(1);
        assertThat(cardTube.getContactMonitoringList().getFirst().getCardTube()).isSameAs(cardTube);
    }

    @Test
    void updateReplacesChildrenInPlaceWithoutReassigningTheCollection() {
        CardTubeRequest initial = requestWith("DISP-1",
                List.of(new XRayRequest(null, LocalDate.now(), "Clinic1", "Clear")),
                List.of(), List.of(), List.of());
        CardTube cardTube = cardWith(initial);
        List<?> originalList = cardTube.getPreMBTChestXRay();

        CardTubeRequest updated = requestWith("DISP-2", List.of(), List.of(), List.of(), List.of());
        handler.update(cardTube, updated);

        assertThat(cardTube.getDispensaryId()).isEqualTo("DISP-2");
        assertThat(cardTube.getPreMBTChestXRay()).isSameAs(originalList).isEmpty();
    }

    @Test
    void toResponseRoundTripsFieldsAndChildren() {
        CardTubeRequest request = requestWith("DISP-1",
                List.of(new XRayRequest(null, LocalDate.now(), "Clinic1", "Clear")),
                List.of(new TBHistoryRequest(null, "Loc1", LocalDate.now(), "MKB1", "Name1", "Group1")),
                List.of(new InfectionSourceRequest(null, "TB1", "John Doe", "REL1", "6 months")),
                List.of(new ContactMonitoringRequest(null, "Jane Doe", LocalDate.now(), 30, "REL1", "Workplace1", "Receiver1", LocalDate.now(), "STATUS1")));
        CardTube cardTube = cardWith(request);

        CardTubeDetailResponse response = handler.toResponse(cardTube);

        assertThat(response.type()).isEqualTo(CardType.CARD_TUBE);
        assertThat(response.status()).isEqualTo(CardStatus.NEW);
        assertThat(response.formId()).isEqualTo(21L);
        assertThat(response.dispensaryId()).isEqualTo("DISP-1");
        assertThat(response.preMBTChestXRay()).hasSize(1);
        assertThat(response.previousTBHistory()).hasSize(1);
        assertThat(response.possibleInfectionSources()).hasSize(1);
        assertThat(response.contactMonitoringList()).hasSize(1);
        assertThat(response.contactMonitoringList().getFirst().fullName()).isEqualTo("Jane Doe");
    }

    private CardTube cardWith(CardTubeRequest request) {
        CardTube cardTube = new CardTube();
        cardTube.setForm058(form);
        handler.update(cardTube, request);
        return cardTube;
    }

    private CardTubeRequest requestWith(
            String dispensaryId,
            List<XRayRequest> preMBTChestXRay,
            List<TBHistoryRequest> previousTBHistory,
            List<InfectionSourceRequest> possibleInfectionSources,
            List<ContactMonitoringRequest> contactMonitoringList
    ) {
        return new CardTubeRequest(
                LocalDate.now(), dispensaryId, "MKB1", "MkbName1",
                LocalDate.now(), "Method1", LocalDate.now(), "HOMESTAY1", LocalDate.now(),
                "VaccineName1", "SN1", null, 2, true,
                preMBTChestXRay, previousTBHistory,
                "Group1", "MKB2", "MkbName2",
                List.of(LocalDate.now()),
                LocalDate.now(), LocalDate.now(), "Dismissal1", LocalDate.now(), "Receiver1", LocalDate.now(),
                List.of("NUTRITION1"),
                "WORK1", "BUDGET1", "HABIT1",
                possibleInfectionSources,
                "HOUSING1", 3, 2, true, 5, 4, 2, 1, 1, 0, 0, 3, 60, 80, 20, 2, 1,
                "SANITARY1", "HEATING1", "SEWERAGE1", true, "RENOVATION1", "HABITABILITY1",
                LocalDate.now(), "PrevDiff1", true, true, 2, true, true, false,
                "DISPOSAL1", "Full Name1", "KINSHIP1", "DISINFECTANT1", 1, "Provider1",
                2, "weeks", 2, "weeks",
                contactMonitoringList,
                "RECOVERY1", LocalDate.now(), LocalDate.now().plusMonths(6)
        );
    }
}
