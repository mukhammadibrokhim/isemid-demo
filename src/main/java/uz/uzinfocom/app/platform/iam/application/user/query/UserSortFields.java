package uz.uzinfocom.app.platform.iam.application.user.query;

import java.util.Map;

public final class UserSortFields {
    public static final Map<String, String> ALLOWED = Map.of(
            "id", "id",
            "firstName", "firstName",
            "lastName", "lastName",
            "middleName", "middleName"
    );
}
