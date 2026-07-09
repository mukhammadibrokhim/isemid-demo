package uz.uzinfocom.app.modules.card.domain.enums;

/**
 * Discriminator values are frontend-facing (JSON "type" field and existing
 * clients) and must not change even though everything else about the legacy
 * card module was rebuilt.
 */
public enum CardType {
    CARD161,
    CARD174,
    CARD175,
    CARD205,
    CARD_TUBE
}
