package uz.uzinfocom.app.platform.iam.application.shared.dto;

import uz.uzinfocom.app.platform.iam.domain.enums.OrganizationLevel;

/** Shared read projection - organization count grouped by levelType, for any caller that needs it. */
public record OrganizationLevelCountProjection(OrganizationLevel levelType, long count) {
}
