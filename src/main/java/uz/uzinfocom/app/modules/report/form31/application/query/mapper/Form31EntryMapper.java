package uz.uzinfocom.app.modules.report.form31.application.query.mapper;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.report.form31.application.query.dto.Form31EntryTableResponse;
import uz.uzinfocom.app.modules.report.form31.domain.Form31Entry;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.modules.reference.application.lookup.ReferenceLookupService;

/**
 * Enriches a persisted {@link Form31Entry} row (which only stores a scalar
 * {@code organizationId}) with the creating organization's display name and
 * hudud (region/district) for the table response — mirrors {@code
 * Form2ManualEntryMapper}.
 */
@Component
public class Form31EntryMapper {

    private final OrganizationNameResolver organizationNameResolver;
    private final ReferenceLookupService referenceLookupService;

    public Form31EntryMapper(
            OrganizationNameResolver organizationNameResolver,
            ReferenceLookupService referenceLookupService
    ) {
        this.organizationNameResolver = organizationNameResolver;
        this.referenceLookupService = referenceLookupService;
    }

    public Form31EntryTableResponse toTableResponse(Form31Entry entity, Organization organization) {
        String regionCode = organization == null ? null : organization.getRegionCode();
        String districtCode = organization == null ? null : organization.getDistrictCode();

        return new Form31EntryTableResponse(
                entity.getId(),
                entity.getOrganizationId(),
                organizationNameResolver.resolve(organization),
                regionCode,
                regionCode == null ? null : referenceLookupService.getRegionName(regionCode),
                districtCode,
                districtCode == null ? null : referenceLookupService.getDistrictName(districtCode),
                entity.getFromDate(),
                entity.getToDate(),
                entity.getIliCasesCount(),
                entity.getAriCasesCount(),
                entity.getPneumoniaCasesCount(),
                entity.getSariTotalCount(),
                entity.getSariPregnantCount(),
                entity.getDeathTotalCount(),
                entity.getDeathPregnantCount(),
                entity.getWeeklyVaccinationCount(),
                entity.getSeasonVaccinationCount(),
                entity.getCreatedAt()
        );
    }
}
