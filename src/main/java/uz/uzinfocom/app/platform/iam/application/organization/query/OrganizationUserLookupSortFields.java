package uz.uzinfocom.app.platform.iam.application.organization.query;

import org.springframework.data.domain.Sort;

import java.util.Map;

public final class OrganizationUserLookupSortFields {

    private OrganizationUserLookupSortFields() {
    }

    public static final String DEFAULT_SORT_BY = "id";
    public static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.DESC;

    public static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
            "id", "id",
            "username", "username",
            "nnuzb", "nnuzb",
            "firstName", "firstName",
            "lastName", "lastName"
    );
}