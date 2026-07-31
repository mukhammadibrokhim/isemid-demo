package uz.uzinfocom.isemid.features.report.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.features.classifier.services.CatalogService;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.reposiotries.OrganizationRepository;
import uz.uzinfocom.isemid.features.patient.models.Patient;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uz.uzinfocom.isemid.commons.utils.DateTimeUtil.formatDate;
import static uz.uzinfocom.isemid.commons.utils.DateTimeUtil.formatDateTime;
import static uz.uzinfocom.isemid.features.report.utils.ReportUtil.*;

@Component
@RequiredArgsConstructor
public class Form058RowMapper {
    private final CatalogService catalogService;
    private final OrganizationRepository organizationRepository;

    public String[] toRow(Form058 form) {
        Patient patient = form.getPatient();

        String fullName = Optional.ofNullable(patient).map(Patient::getFullName).orElse("");

        String ageString = resolveAge(
                (patient != null) ? patient.getBirthDate() : null,
                (patient != null) ? patient.getAgeYears() : null,
                (patient != null) ? patient.getAgeMonths() : null
        );

        String address = resolveAddresses(form, catalogService);

        String icd10 = (form.getMkb10Code() == null ? "" : form.getMkb10Code()) + "-" +
                (form.getMkb10Name() == null ? "" : form.getMkb10Name());

        String icd10Final = (form.getFinalMkb10Code() == null ? "" : form.getFinalMkb10Code()) + "-" +
                (form.getFinalMkb10Name() == null ? "" : form.getFinalMkb10Name());

        String hospitalAndDate = resolveHospitalAndDate(form, organizationRepository);
        String createdOrgAndDocName = resolveCreatedOrgAndDocName(form);
        String receiverOrg = resolveOrganizationName(form.getReceiverOrganizationId(), organizationRepository);

        return new String[]{
                String.valueOf(form.getId()),
                formatDateTime(form.getCreatedAt()),
                Optional.ofNullable(form.getCreatedOrg()).map(Organization::getName).orElse(""),
                fullName,
                ageString,
                address,
                "", // Work/study place (reserved)
                formatDate(form.getDiseaseDate()),
                formatDateTime(form.getFirstVisitDate()),
                hospitalAndDate,
                String.join(", ", icd10, formatDate(form.getDiagnosisDate())),
                String.join(", ", icd10Final, formatDate(form.getDiagnosisDate())),
                createdOrgAndDocName,
                receiverOrg,
                "", // Lab result
                ""  // Notes
        };
    }

    /**
     * N+1'siz variant: {@code toRow}dan farqli, bu metod har chaqiruvda DB'ga
     * bormaydi — barcha bog'liq ma'lumotlar (bemor, tashkilotlar, katalog nomlari)
     * chaqiruvchi tomonidan batch qilib oldindan yuklab, cache (Map) sifatida uzatiladi.
     */
    public String[] toRowV2(
            Form058 form,
            Map<Long, Patient> patientCache,
            Map<Long, Organization> orgByIdCache,
            Map<UUID, Organization> orgByUuidCache,
            Map<String, String> catalogCache
    ) {
        Patient patient = Optional.ofNullable(form.getPatient())
                .map(p -> patientCache.get(p.getId()))
                .orElse(null);

        String fullName = Optional.ofNullable(patient).map(Patient::getFullName).orElse("");

        String ageString = resolveAge(
                (patient != null) ? patient.getBirthDate() : null,
                (patient != null) ? patient.getAgeYears() : null,
                (patient != null) ? patient.getAgeMonths() : null
        );

        String address = resolveAddressesV2(patient, catalogCache);

        String icd10 = (form.getMkb10Code() == null ? "" : form.getMkb10Code()) + "-" +
                (form.getMkb10Name() == null ? "" : form.getMkb10Name());

        String icd10Final = (form.getFinalMkb10Code() == null ? "" : form.getFinalMkb10Code()) + "-" +
                (form.getFinalMkb10Name() == null ? "" : form.getFinalMkb10Name());

        String hospitalAndDate = resolveHospitalAndDateV2(form, orgByIdCache);
        String createdOrgAndDocName = resolveCreatedOrgAndDocNameV2(form, orgByUuidCache);
        String receiverOrg = resolveOrganizationNameV2(form.getReceiverOrganizationId(), orgByIdCache);

        String createdOrgName = Optional.ofNullable(form.getCreatedOrgUuid())
                .map(orgByUuidCache::get)
                .map(Organization::getName)
                .orElse("");

        return new String[]{
                String.valueOf(form.getId()),
                formatDateTime(form.getCreatedAt()),
                createdOrgName,
                fullName,
                ageString,
                address,
                "", // Work/study place (reserved)
                formatDate(form.getDiseaseDate()),
                formatDateTime(form.getFirstVisitDate()),
                hospitalAndDate,
                String.join(", ", icd10, formatDate(form.getDiagnosisDate())),
                String.join(", ", icd10Final, formatDate(form.getDiagnosisDate())),
                createdOrgAndDocName,
                receiverOrg,
                "", // Lab result
                ""  // Notes
        };
    }
}