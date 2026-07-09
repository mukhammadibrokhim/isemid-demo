package uz.uzinfocom.app.modules.card.application.handler.card205;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.Card205DetailResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.card205.Card205;
import uz.uzinfocom.app.modules.card.mapper.card205.Card205MapperImpl;
import uz.uzinfocom.app.modules.card.web.dto.request.Card205Request;
import uz.uzinfocom.app.modules.card.web.dto.request.card205.InformationAboutAnimaBittenPeopleRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card205.InformationOtherBittenAnimalsRequest;
import uz.uzinfocom.app.modules.card.web.dto.request.card205.InformationOtherBittenPeopleRequest;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Card205HandlerTest {

    private Card205Handler handler;
    private Form058 form;

    @BeforeEach
    void setUp() {
        handler = new Card205Handler(new Card205MapperImpl());

        form = mock(Form058.class);
        when(form.getId()).thenReturn(11L);
    }

    @Test
    void createBuildsEntityGraphAndWiresBackReferences() {
        Card205Request request = requestWith("MKB-1",
                List.of(new InformationOtherBittenPeopleRequest("Doe", "John", null, "M", "1990", null, null, null, null, null, null, null, null)),
                List.of(new InformationOtherBittenAnimalsRequest("CAT1", null, "Somewhere")),
                List.of(new InformationAboutAnimaBittenPeopleRequest("CAT2", "Dog", "Owner Name", null, null, null, null, null, null, null)));

        Card205 card205 = handler.create(form, request);

        assertThat(card205.getForm058()).isSameAs(form);
        assertThat(card205.getCardType()).isEqualTo(CardType.CARD205);
        assertThat(card205.getMkb10Code()).isEqualTo("MKB-1");

        assertThat(card205.getInfoBittenPeople()).hasSize(1);
        assertThat(card205.getInfoBittenPeople().getFirst().getCard205()).isSameAs(card205);

        assertThat(card205.getInfoOtherBittenAnimal()).hasSize(1);
        assertThat(card205.getInfoOtherBittenAnimal().getFirst().getCard205()).isSameAs(card205);

        assertThat(card205.getInfoAbtAnimalBittenPeople()).hasSize(1);
        assertThat(card205.getInfoAbtAnimalBittenPeople().getFirst().getCard205()).isSameAs(card205);
    }

    @Test
    void updateReplacesChildrenInPlaceWithoutReassigningTheCollection() {
        Card205Request initial = requestWith("MKB-1",
                List.of(new InformationOtherBittenPeopleRequest("Doe", "John", null, "M", "1990", null, null, null, null, null, null, null, null)),
                List.of(new InformationOtherBittenAnimalsRequest("CAT1", null, "Somewhere")),
                List.of(new InformationAboutAnimaBittenPeopleRequest("CAT2", "Dog", "Owner Name", null, null, null, null, null, null, null)));
        Card205 card205 = handler.create(form, initial);
        List<?> originalList = card205.getInfoBittenPeople();

        Card205Request updated = requestWith("MKB-2", List.of(), List.of(), List.of());
        handler.update(card205, updated);

        assertThat(card205.getMkb10Code()).isEqualTo("MKB-2");
        assertThat(card205.getInfoBittenPeople()).isSameAs(originalList).isEmpty();
        assertThat(card205.getInfoOtherBittenAnimal()).isEmpty();
        assertThat(card205.getInfoAbtAnimalBittenPeople()).isEmpty();
    }

    @Test
    void toResponseRoundTripsFieldsAndChildren() {
        Card205Request request = requestWith("MKB-1",
                List.of(new InformationOtherBittenPeopleRequest("Doe", "John", null, "M", "1990", null, null, null, null, null, null, null, null)),
                List.of(new InformationOtherBittenAnimalsRequest("CAT1", null, "Somewhere")),
                List.of(new InformationAboutAnimaBittenPeopleRequest("CAT2", "Dog", "Owner Name", null, null, null, null, null, null, null)));
        Card205 card205 = handler.create(form, request);

        Card205DetailResponse response = handler.toResponse(card205);

        assertThat(response.type()).isEqualTo(CardType.CARD205);
        assertThat(response.status()).isEqualTo(CardStatus.NEW);
        assertThat(response.formId()).isEqualTo(11L);
        assertThat(response.mkb10Code()).isEqualTo("MKB-1");
        assertThat(response.infoBittenPeople()).hasSize(1);
        assertThat(response.infoBittenPeople().getFirst().lastName()).isEqualTo("Doe");
        assertThat(response.infoOtherBittenAnimal()).hasSize(1);
        assertThat(response.infoAbtAnimalBittenPeople()).hasSize(1);
        assertThat(response.infoAbtAnimalBittenPeople().getFirst().fullNameOfAnimalBittenOwner()).isEqualTo("Owner Name");
    }

    private Card205Request requestWith(
            String mkb10Code,
            List<InformationOtherBittenPeopleRequest> infoBittenPeople,
            List<InformationOtherBittenAnimalsRequest> infoOtherBittenAnimal,
            List<InformationAboutAnimaBittenPeopleRequest> infoAbtAnimalBittenPeople
    ) {
        return new Card205Request(
                mkb10Code, "Name",
                LocalDate.now(), LocalDate.now(),
                "BiteAddress", LocalDate.now(),
                "Institution", LocalDate.now(),
                infoBittenPeople,
                "AnimalType1", "Somewhere", "Recently",
                "CONDITION1", 3, "Breed1", "Colour1", "Signs1",
                1, LocalDate.now(),
                "CONSERVATION1", "POSITION1",
                2, LocalDate.now(),
                "FeatherInfo", "PetDept1", "COMPLIANCE1",
                infoOtherBittenAnimal,
                infoAbtAnimalBittenPeople,
                "AdditionalInfo", "Animal Owner Name"
        );
    }
}
