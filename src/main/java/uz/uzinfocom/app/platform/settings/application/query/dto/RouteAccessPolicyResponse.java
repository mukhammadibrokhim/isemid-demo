package uz.uzinfocom.app.platform.settings.application.query.dto;

public record RouteAccessPolicyResponse(
        Long id,
        String pattern,
        Integer displayOrder,
        Boolean open,
        Boolean organizationHeaderRequired,
        Boolean roleValidationRequired,
        Boolean enabled,
        String description
) {
}
