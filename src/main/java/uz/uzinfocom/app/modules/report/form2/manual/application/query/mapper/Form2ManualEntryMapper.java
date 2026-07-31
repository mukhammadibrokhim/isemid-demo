package uz.uzinfocom.app.modules.report.form2.manual.application.query.mapper;

import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.report.form2.manual.application.query.dto.Form2ManualEntryTableResponse;
import uz.uzinfocom.app.modules.report.form2.manual.domain.Form2ManualEntry;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationNameResolver;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.reference.application.lookup.ReferenceLookupService;

/**
 * Enriches a persisted {@link Form2ManualEntry} row (which only stores a
 * scalar {@code organizationId}) with the creating organization's display
 * name and hudud (region/district) for the table response — mirrors how
 * {@code ReportHierarchyService} resolves geography names elsewhere in
 * {@code modules.report}.
 */
@Component
public class Form2ManualEntryMapper {

    private final OrganizationNameResolver organizationNameResolver;
    private final ReferenceLookupService referenceLookupService;

    public Form2ManualEntryMapper(
            OrganizationNameResolver organizationNameResolver,
            ReferenceLookupService referenceLookupService
    ) {
        this.organizationNameResolver = organizationNameResolver;
        this.referenceLookupService = referenceLookupService;
    }

    public Form2ManualEntryTableResponse toTableResponse(Form2ManualEntry entity, Organization organization) {
        String regionCode = organization == null ? null : organization.getRegionCode();
        String districtCode = organization == null ? null : organization.getDistrictCode();

        return new Form2ManualEntryTableResponse(
                entity.getId(),
                entity.getOrganizationId(),
                organizationNameResolver.resolve(organization),
                regionCode,
                regionCode == null ? null : referenceLookupService.getRegionName(regionCode),
                districtCode,
                districtCode == null ? null : referenceLookupService.getDistrictName(districtCode),
                entity.getFromDate(),
                entity.getToDate(),
                entity.getRegisteredCasesLastYear(),
                entity.getRegisteredCasesCurrentYear(),
                entity.getMtmSchoolCount(),
                entity.getDisinfectionFociCount(),
                entity.getInspectedObjectsCount(),
                entity.getClosedObjectsCount(),
                entity.getDduCount(),
                entity.getSchoolCount(),
                entity.getFinesCount(),
                entity.getProsecutorReferralCount(),
                entity.getCreatedAt()
        );
    }
}
