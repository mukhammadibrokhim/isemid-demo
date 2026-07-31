package uz.uzinfocom.app.modules.report.form32.application.query.mapper;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.report.form32.application.query.dto.Form32EntryTableResponse;
import uz.uzinfocom.app.modules.report.form32.domain.Form32Entry;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceLookupService;

/**
 * Enriches a persisted {@link Form32Entry} row (which only stores a scalar
 * {@code organizationId}) with the creating organization's display name and
 * hudud (region/district) for the table response — mirrors {@code
 * Form31EntryMapper}.
 */
@Component
public class Form32EntryMapper {

    private final OrganizationNameResolver organizationNameResolver;
    private final ReferenceLookupService referenceLookupService;

    public Form32EntryMapper(
            OrganizationNameResolver organizationNameResolver,
            ReferenceLookupService referenceLookupService
    ) {
        this.organizationNameResolver = organizationNameResolver;
        this.referenceLookupService = referenceLookupService;
    }

    public Form32EntryTableResponse toTableResponse(Form32Entry entity, Organization organization) {
        String regionCode = organization == null ? null : organization.getRegionCode();
        String districtCode = organization == null ? null : organization.getDistrictCode();

        return new Form32EntryTableResponse(
                entity.getId(),
                entity.getOrganizationId(),
                organizationNameResolver.resolve(organization),
                regionCode,
                regionCode == null ? null : referenceLookupService.getRegionName(regionCode),
                districtCode,
                districtCode == null ? null : referenceLookupService.getDistrictName(districtCode),
                entity.getFromDate(),
                entity.getToDate(),
                entity.getInspectedTotalCount(),
                entity.getInspectedMtmCount(),
                entity.getInspectedSchoolCount(),
                entity.getInspectedDpmCount(),
                entity.getInspectedOtherCount(),
                entity.getDeficiencyTotalCount(),
                entity.getDeficiencyMtmCount(),
                entity.getDeficiencySchoolCount(),
                entity.getDeficiencyDpmCount(),
                entity.getDeficiencyOtherCount(),
                entity.getOfficialTotalCount(),
                entity.getOfficialMtmCount(),
                entity.getOfficialSchoolCount(),
                entity.getOfficialDpmCount(),
                entity.getOfficialOtherCount(),
                entity.getSuspendedTotalCount(),
                entity.getSuspendedMtmCount(),
                entity.getSuspendedSchoolCount(),
                entity.getSuspendedDpmCount(),
                entity.getSuspendedOtherCount(),
                entity.getCreatedAt()
        );
    }
}
