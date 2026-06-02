package uz.uzinfocom.app.platform.security.route;

public record RequestPolicy(
        boolean publicRoute,
        boolean organizationHeaderRequired,
        boolean roleValidationRequired,
        String matchedPattern
) {

    public static RequestPolicy publicRoute(String matchedPattern) {
        return new RequestPolicy(true, false, false, matchedPattern);
    }

    public static RequestPolicy defaultProtectedRoute() {
        return new RequestPolicy(false, true, true, "<default>");
    }
}
