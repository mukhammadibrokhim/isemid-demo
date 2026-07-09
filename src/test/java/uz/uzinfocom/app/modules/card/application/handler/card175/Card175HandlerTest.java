package uz.uzinfocom.app.modules.card.application.handler.card175;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.Card175DetailResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CardStatus;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;
import uz.uzinfocom.app.modules.card.domain.model.card175.Card175;
import uz.uzinfocom.app.modules.card.mapper.card175.Card175MapperImpl;
import uz.uzinfocom.app.modules.card.web.dto.request.Card175Request;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Card175HandlerTest {

    private Card175Handler handler;
    private Form058 form;

    @BeforeEach
    void setUp() {
        handler = new Card175Handler(new Card175MapperImpl());

        form = mock(Form058.class);
        when(form.getId()).thenReturn(7L);
    }

    @Test
    void createBuildsEntity() {
        Card175Request request = requestWith("PATHOGEN1", List.of("INJURY1", "INJURY2"));

        Card175 card175 = handler.create(form, request);

        assertThat(card175.getForm058()).isSameAs(form);
        assertThat(card175.getCardType()).isEqualTo(CardType.CARD175);
        assertThat(card175.getPathogenType()).isEqualTo("PATHOGEN1");
        assertThat(card175.getPartOfInjury()).containsExactly("INJURY1", "INJURY2");
    }

    @Test
    void updateOverwritesScalarAndCollectionFields() {
        Card175 card175 = handler.create(form, requestWith("PATHOGEN1", List.of("INJURY1")));

        handler.update(card175, requestWith("PATHOGEN2", List.of()));

        assertThat(card175.getPathogenType()).isEqualTo("PATHOGEN2");
        assertThat(card175.getPartOfInjury()).isEmpty();
    }

    @Test
    void toResponseRoundTripsFields() {
        Card175 card175 = handler.create(form, requestWith("PATHOGEN1", List.of("INJURY1")));

        Card175DetailResponse response = handler.toResponse(card175);

        assertThat(response.type()).isEqualTo(CardType.CARD175);
        assertThat(response.status()).isEqualTo(CardStatus.NEW);
        assertThat(response.formId()).isEqualTo(7L);
        assertThat(response.pathogenType()).isEqualTo("PATHOGEN1");
        assertThat(response.partOfInjury()).containsExactly("INJURY1");
    }

    private Card175Request requestWith(String pathogenType, List<String> partOfInjury) {
        return new Card175Request(
                LocalDateTime.now(), LocalDate.now(), LocalDateTime.now(), LocalDateTime.now(),
                LocalDate.now(), LocalDate.now(),
                pathogenType,
                "WHERE1", "COME1", "DISCHARGE1", "TRANSPORT1", "LEAVING1", "LATE1",
                "CONFIRMED1", "VACCINATION1", "LastVaccinationInfo", "EpidemComment",
                "INITIALDX1", "IDENTIFIED1", "ApplicationPlace", "PREVENTION1",
                "MedicineName", 10L, 20L, 3L, "ClinicalForm1",
                partOfInjury,
                "SEVERITY1", "RELEVANCE1", "SPREADER1", "OWNER1", "OBSERVATION1", "CHECKING1",
                List.of("COND1"), List.of("FACTOR1"), List.of("MEASURE1")
        );
    }
}
