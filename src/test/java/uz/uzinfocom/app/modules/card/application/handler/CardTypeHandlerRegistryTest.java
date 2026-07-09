package uz.uzinfocom.app.modules.card.application.handler;

import org.junit.jupiter.api.Test;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CardTypeHandlerRegistryTest {

    @Test
    void resolvesHandlerForEachRegisteredType() {
        CardTypeHandler<?, ?, ?> card161Handler = handlerFor(CardType.CARD161);
        CardTypeHandler<?, ?, ?> cardTubeHandler = handlerFor(CardType.CARD_TUBE);

        CardTypeHandlerRegistry registry = new CardTypeHandlerRegistry(List.of(
                card161Handler,
                handlerFor(CardType.CARD174),
                handlerFor(CardType.CARD175),
                handlerFor(CardType.CARD205),
                cardTubeHandler
        ));

        assertThat(registry.get(CardType.CARD161)).isSameAs(card161Handler);
        assertThat(registry.get(CardType.CARD_TUBE)).isSameAs(cardTubeHandler);
    }

    @Test
    void failsFastWhenATypeHasNoHandler() {
        List<CardTypeHandler<?, ?, ?>> incomplete = List.of(
                handlerFor(CardType.CARD161),
                handlerFor(CardType.CARD175),
                handlerFor(CardType.CARD205),
                handlerFor(CardType.CARD_TUBE)
        );

        assertThatThrownBy(() -> new CardTypeHandlerRegistry(incomplete))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CARD174");
    }

    @Test
    void failsFastWhenTwoHandlersClaimTheSameType() {
        List<CardTypeHandler<?, ?, ?>> duplicated = List.of(
                handlerFor(CardType.CARD161),
                handlerFor(CardType.CARD161),
                handlerFor(CardType.CARD174),
                handlerFor(CardType.CARD175),
                handlerFor(CardType.CARD205),
                handlerFor(CardType.CARD_TUBE)
        );

        assertThatThrownBy(() -> new CardTypeHandlerRegistry(duplicated))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate");
    }

    private CardTypeHandler<?, ?, ?> handlerFor(CardType type) {
        CardTypeHandler<?, ?, ?> handler = mock(CardTypeHandler.class);
        when(handler.getType()).thenReturn(type);
        return handler;
    }
}
