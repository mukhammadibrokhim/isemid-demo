package uz.uzinfocom.app.platform.iam.application.shared.dto;

/**
 * Pairs an organization id with a single geographic code (its region or
 * district code, depending on which repository query produced it) — used to
 * bucket per-organization counts into per-region/per-district totals for the
 * home dashboard's geographic breakdown.
 */
public record OrganizationGeoProjection(
        Long id,
        String code
) {
}
