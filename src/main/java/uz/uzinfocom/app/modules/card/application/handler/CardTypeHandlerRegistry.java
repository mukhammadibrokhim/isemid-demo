package uz.uzinfocom.app.modules.card.application.handler;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.card.application.exception.UnsupportedCardTypeException;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Fails fast at startup if any {@link CardType} is missing a handler, or if
 * two handlers claim the same type — a wiring bug like that must be caught
 * immediately, not surface later as a runtime 500 on whichever type was
 * forgotten. Adding a 6th card type only requires a new
 * {@link CardTypeHandler} bean; nothing here needs to change.
 */
@Component
public class CardTypeHandlerRegistry {

    private final Map<CardType, CardTypeHandler<?, ?, ?>> handlersByType;

    public CardTypeHandlerRegistry(List<CardTypeHandler<?, ?, ?>> handlers) {
        Map<CardType, CardTypeHandler<?, ?, ?>> byType = new EnumMap<>(CardType.class);

        for (CardTypeHandler<?, ?, ?> handler : handlers) {
            CardType type = handler.getType();
            CardTypeHandler<?, ?, ?> existing = byType.putIfAbsent(type, handler);
            if (existing != null) {
                throw new IllegalStateException(
                        "Duplicate CardTypeHandler for %s: %s and %s".formatted(
                                type, existing.getClass().getName(), handler.getClass().getName())
                );
            }
        }

        List<CardType> missing = Arrays.stream(CardType.values())
                .filter(type -> !byType.containsKey(type))
                .toList();

        if (!missing.isEmpty()) {
            throw new IllegalStateException("No CardTypeHandler registered for: " + missing);
        }

        this.handlersByType = Collections.unmodifiableMap(byType);
    }

    public CardTypeHandler<?, ?, ?> get(CardType type) {
        CardTypeHandler<?, ?, ?> handler = handlersByType.get(type);
        if (handler == null) {
            throw new UnsupportedCardTypeException(type);
        }
        return handler;
    }
}
