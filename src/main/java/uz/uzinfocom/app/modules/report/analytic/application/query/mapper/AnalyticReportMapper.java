package uz.uzinfocom.app.modules.report.analytic.application.query.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportResponse;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportTableResponse;
import uz.uzinfocom.app.modules.report.analytic.domain.AnalyticReport;

/**
 * Enriches a persisted {@link AnalyticReport} row (which only stores a scalar
 * {@code organizationId}) with the creating organization's display name —
 * mirrors {@code Form2ManualEntryMapper}.
 */
@Component
@RequiredArgsConstructor
public class AnalyticReportMapper {

    private final OrganizationNameResolver organizationNameResolver;

    public AnalyticReportTableResponse toTableResponse(AnalyticReport entity, Organization organization) {
        return new AnalyticReportTableResponse(
                entity.getId(),
                entity.getName(),
                entity.getStatus(),
                entity.getFromDate(),
                entity.getToDate(),
                entity.getOrganizationId(),
                organizationNameResolver.resolve(organization),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public AnalyticReportResponse toResponse(AnalyticReport entity, Organization organization) {
        return new AnalyticReportResponse(
                entity.getId(),
                entity.getName(),
                entity.getStatus(),
                entity.getFromDate(),
                entity.getToDate(),
                entity.getRegionCodes(),
                entity.getIcd10Codes(),
                entity.getKoef(),
                entity.getContent(),
                entity.getOrganizationId(),
                organizationNameResolver.resolve(organization),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
