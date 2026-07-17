package uz.uzinfocom.isemid.features.card.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uz.uzinfocom.isemid.features.card.enums.CardStatus;
import uz.uzinfocom.isemid.features.card.enums.CardType;
import uz.uzinfocom.isemid.features.card.models.card161.Card161;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form0581.models.Form0581;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Guards against the exact failure mode the plan flagged as a real runtime risk:
 * Card now carries two @JsonBackReference fields (form058, form0581) pointing at two
 * different parent types. Jackson requires each managed/back-reference PAIR to share a
 * unique name, or it throws InvalidDefinitionException at introspection time (not caught
 * by javac). This test forces Jackson to introspect both Form058.cards<->Card.form058 and
 * Form0581.cards<->Card.form0581 in the same JVM, which is exactly when a naming collision
 * would surface.
 */
class CardJsonReferenceTest {

    @Test
    void form058AndForm0581BothSerializeWithoutJacksonReferenceCollision() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Form058 form058 = new Form058();
        Card161 card161 = new Card161();
        card161.setCardType(CardType.CARD161);
        card161.setStatus(CardStatus.NEW);
        card161.setForm058(form058);
        form058.setCards(java.util.List.of(card161));

        Form0581 form0581 = new Form0581();
        Card161 card174Like = new Card161();
        card174Like.setCardType(CardType.CARD161);
        card174Like.setStatus(CardStatus.NEW);
        card174Like.setForm0581(form0581);
        form0581.setCards(java.util.List.of(card174Like));

        assertThatCode(() -> mapper.writeValueAsString(form058)).doesNotThrowAnyException();
        assertThatCode(() -> mapper.writeValueAsString(form0581)).doesNotThrowAnyException();
        assertThatCode(() -> mapper.writeValueAsString(card161)).doesNotThrowAnyException();
        assertThatCode(() -> mapper.writeValueAsString(card174Like)).doesNotThrowAnyException();
    }
}
