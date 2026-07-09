package uz.uzinfocom.app.modules.card.application.query;

import java.util.Map;

public final class CardSortFields {

    public static final Map<String, String> ALLOWED = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("cardType", "cardType"),
            Map.entry("status", "status"),
            Map.entry("createdAt", "createdAt"),
            Map.entry("updatedAt", "updatedAt")
    );

    private CardSortFields() {
    }
}
