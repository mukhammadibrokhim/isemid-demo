package uz.uzinfocom.app.platform.integrationclient.application.query;

import java.util.Map;

public class IntegrationClientSortFields {
    public static final Map<String, String> ALLOWED = Map.of(
            "id", "id",
            "name", "name",
            "clientId", "clientId",
            "sourceKey", "sourceKey",
            "authType", "authType",
            "active", "active",
            "lastUsedAt", "lastUsedAt",
            "createdAt", "createdAt"
    );
}
