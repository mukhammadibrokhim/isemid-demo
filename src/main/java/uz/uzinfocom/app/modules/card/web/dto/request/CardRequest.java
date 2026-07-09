package uz.uzinfocom.app.modules.card.web.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import uz.uzinfocom.app.modules.card.domain.enums.CardType;

/**
 * Common contract for every per-type create/update request. {@code permits}
 * grows by one as each remaining card type is built (Phase 3) — the
 * compiler then enforces that every switch/pattern-match over
 * {@code CardRequest} stays exhaustive. Polymorphism lives only here, in the
 * DTO layer — the JPA entities have no Jackson annotations at all.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Card161Request.class, name = "CARD161"),
        @JsonSubTypes.Type(value = Card174Request.class, name = "CARD174"),
        @JsonSubTypes.Type(value = Card175Request.class, name = "CARD175"),
        @JsonSubTypes.Type(value = Card205Request.class, name = "CARD205"),
        @JsonSubTypes.Type(value = CardTubeRequest.class, name = "CARD_TUBE")
})
public sealed interface CardRequest permits Card161Request, Card174Request, Card175Request, Card205Request, CardTubeRequest {

    CardType type();
}
