package uz.uzinfocom.app.modules.report.form7.application.query.mapper;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;
import uz.uzinfocom.app.modules.report.form7.application.query.dto.Form7EntryTableResponse;
import uz.uzinfocom.app.modules.report.form7.domain.Form7Entry;

/**
 * Enriches a persisted {@link Form7Entry} row (which only stores a scalar
 * {@code organizationId}) with the creating organization's display name and
 * hudud (region/district) for the table response, and derives the
 * period-over-period case change — mirrors {@code Form2ManualEntryMapper}.
 */
@Component
public class Form7EntryMapper {

    private final OrganizationNameResolver organizationNameResolver;
    private final ReferenceLookupService referenceLookupService;

    public Form7EntryMapper(
            OrganizationNameResolver organizationNameResolver,
            ReferenceLookupService referenceLookupService
    ) {
        this.organizationNameResolver = organizationNameResolver;
        this.referenceLookupService = referenceLookupService;
    }

    public Form7EntryTableResponse toTableResponse(Form7Entry entity, Organization organization) {
        String regionCode = organization == null ? null : organization.getRegionCode();
        String districtCode = organization == null ? null : organization.getDistrictCode();

        return new Form7EntryTableResponse(
                entity.getId(),
                entity.getOrganizationId(),
                organizationNameResolver.resolve(organization),
                regionCode,
                regionCode == null ? null : referenceLookupService.getRegionName(regionCode),
                districtCode,
                districtCode == null ? null : referenceLookupService.getDistrictName(districtCode),
                entity.getFromDate(),
                entity.getToDate(),
                entity.getCasesAtPeriodStart(),
                entity.getRegisteredTotal(),
                entity.getRegisteredUnder14(),
                entity.getRegisteredUnder18(),
                entity.getRegisteredAdult(),
                entity.getRegisteredFemale(),
                entity.getRegisteredUrbanCount(),
                entity.getRegisteredRuralCount(),
                entity.getExaminedCount(),
                entity.getToBeExaminedCount(),
                entity.getPrimaryDiagnosisConfirmed(),
                entity.getHospitalizedCount(),
                entity.getCasesAtPeriodEnd(),
                caseChange(entity),
                entity.getCreatedAt()
        );
    }

    private Integer caseChange(Form7Entry entity) {
        int start = entity.getCasesAtPeriodStart() == null ? 0 : entity.getCasesAtPeriodStart();
        int end = entity.getCasesAtPeriodEnd() == null ? 0 : entity.getCasesAtPeriodEnd();
        return end - start;
    }
}
