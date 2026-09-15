package uz.uzinfocom.app.platform.integrationclient.application.query;

import java.util.Map;

public class IntegrationClientSortFields {
    public static final Map<String, String> ALLOWED = Map.of(
            "id", "id",
            "name", "name",
            "clientId", "clientId",
            "organizationId", "organizationId",
            "createdAt", "createdAt"
    );
}
