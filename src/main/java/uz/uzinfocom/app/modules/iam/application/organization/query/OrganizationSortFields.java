package uz.uzinfocom.app.modules.iam.application.organization.query;

import java.util.Map;

public class OrganizationSortFields {
    public static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
            "id", "id",
            "name", "name",
            "regionCode", "regionCode",
            "districtCode", "districtCode"

    );
}
