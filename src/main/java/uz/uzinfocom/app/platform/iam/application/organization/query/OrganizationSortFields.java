package uz.uzinfocom.app.platform.iam.application.organization.query;

import java.util.Map;

public class OrganizationSortFields {
    public static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
            "id", "id",
            "name", "name",
            "stateCode", "stateCode",
            "cityCode", "cityCode"

    );
}
