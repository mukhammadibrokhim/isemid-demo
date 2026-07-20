package uz.uzinfocom.app.platform.iam.application.shared.dto;

/** Shared read projection - active-user count grouped by role name, for any caller that needs it. */
public record RoleUserCountProjection(String roleName, long count) {
}
