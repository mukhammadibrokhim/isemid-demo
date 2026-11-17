package uz.uzinfocom.app.modules.form129.application.query;

import java.util.Map;

public final class Form129SortFields {

    public static final Map<String, String> ALLOWED = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("status", "status"),
            Map.entry("senderOrganizationId", "senderOrganizationId"),
            Map.entry("receiverOrganizationId", "receiverOrganizationId"),
            Map.entry("createdAt", "createdAt"),
            Map.entry("updatedAt", "updatedAt")
    );

    private Form129SortFields() {
    }
}
