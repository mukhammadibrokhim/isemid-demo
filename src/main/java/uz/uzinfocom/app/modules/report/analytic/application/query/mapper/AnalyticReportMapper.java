package uz.uzinfocom.app.modules.report.analytic.application.query.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportResponse;
import uz.uzinfocom.app.modules.report.analytic.application.query.dto.AnalyticReportTableResponse;
import uz.uzinfocom.app.modules.report.analytic.domain.AnalyticReport;

import java.util.LinkedHashSet;
import java.util.Set;

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
                copyOf(entity.getRegionCodes()),
                copyOf(entity.getIcd10Codes()),
                entity.getKoef(),
                entity.getContent(),
                entity.getOrganizationId(),
                organizationNameResolver.resolve(organization),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Materializes the entity's lazy {@code @ElementCollection} into a plain
     * {@code Set} while the persistence context is still open — the caller
     * (e.g. {@code GET /{id}}'s controller, or {@code AnalyticReportDocxExportService})
     * reads the DTO after the transaction has closed, and {@code open-in-view=false}
     * means a Hibernate-backed proxy would throw {@code LazyInitializationException}
     * at that point instead.
     */
    private Set<String> copyOf(Set<String> lazyBackedSet) {
        return new LinkedHashSet<>(lazyBackedSet);
    }
}
