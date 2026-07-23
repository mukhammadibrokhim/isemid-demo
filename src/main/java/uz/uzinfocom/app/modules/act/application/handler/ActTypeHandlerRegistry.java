package uz.uzinfocom.app.modules.act.application.handler;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.act.application.exception.UnsupportedActTypeException;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Fails fast at startup if any {@link ActType} is missing a handler, or if
 * two handlers claim the same type. Mirrors
 * {@code uz.uzinfocom.app.modules.card.application.handler.CardTypeHandlerRegistry}
 * exactly.
 */
@Component
public class ActTypeHandlerRegistry {

    private final Map<ActType, ActTypeHandler<?, ?, ?>> handlersByType;

    public ActTypeHandlerRegistry(List<ActTypeHandler<?, ?, ?>> handlers) {
        Map<ActType, ActTypeHandler<?, ?, ?>> byType = new EnumMap<>(ActType.class);

        for (ActTypeHandler<?, ?, ?> handler : handlers) {
            ActType type = handler.getType();
            ActTypeHandler<?, ?, ?> existing = byType.putIfAbsent(type, handler);
            if (existing != null) {
                throw new IllegalStateException(
                        "Duplicate ActTypeHandler for %s: %s and %s".formatted(
                                type, existing.getClass().getName(), handler.getClass().getName())
                );
            }
        }

        List<ActType> missing = Arrays.stream(ActType.values())
                .filter(type -> !byType.containsKey(type))
                .toList();

        if (!missing.isEmpty()) {
            throw new IllegalStateException("No ActTypeHandler registered for: " + missing);
        }

        this.handlersByType = Collections.unmodifiableMap(byType);
    }

    public ActTypeHandler<?, ?, ?> get(ActType type) {
        ActTypeHandler<?, ?, ?> handler = handlersByType.get(type);
        if (handler == null) {
            throw new UnsupportedActTypeException(type);
        }
        return handler;
    }
}
