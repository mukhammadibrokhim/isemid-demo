package uz.uzinfocom.app.modules.form058.application.query.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTableResponse;
import uz.uzinfocom.app.modules.form058.application.query.dto.pdf.Form058PdfAddressResponse;
import uz.uzinfocom.app.modules.form058.application.query.dto.pdf.Form058PdfPatientResponse;
import uz.uzinfocom.app.modules.form058.application.query.dto.pdf.Form058PdfResponse;
import uz.uzinfocom.app.modules.form058.application.query.dto.pdf.Form058PdfWorkplaceResponse;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058ClinicalInfo;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058EpidemicInfo;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058ReportInfo;
import uz.uzinfocom.app.modules.patient.domain.enums.AddressType;
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.model.PatientAddress;
import uz.uzinfocom.app.modules.patient.domain.model.PatientIdentifier;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationMappingHelper;
import uz.uzinfocom.app.platform.iam.application.user.query.dto.UserMiniResponse;
import uz.uzinfocom.app.platform.iam.application.user.query.mapper.UserMapperHelper;
import uz.uzinfocom.app.platform.reference.application.lookup.mapper.ReferenceMappingHelper;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builds the print-oriented view of a Form058: every coded field (region,
 * gender, marital status, profession, disease place, organizations) is
 * resolved to its display name here, unlike {@link Form058DetailResponseMapper}
 * which returns raw catalog codes for API consumers that already have their
 * own catalog cache.
 */
@Component
@RequiredArgsConstructor
public class Form058PdfMapper {

    /**
     * Two identifier type-code conventions coexist in the real data (PINFL/PASSPORT is the
     * dominant one, NNUZB/PPN a minority) - both are matched so the PDF resolves either way.
     */
    private static final Set<String> IDENTIFIER_TYPES_PINFL = Set.of("PINFL", "NNUZB");
    private static final Set<String> IDENTIFIER_TYPES_PASSPORT = Set.of("PASSPORT", "PPN");

    private final Form058DetailResponseMapper form058DetailResponseMapper;
    private final ReferenceMappingHelper referenceMappingHelper;
    private final OrganizationMappingHelper organizationMappingHelper;
    private final UserMapperHelper userMapperHelper;

    /**
     * {@code cards} is the same list already fetched for the linked-cards section of the
     * detail response - passed in here rather than re-queried, since the person who attached
     * a card to the form is treated as the one who received it (see {@link #resolveReceiverFullName}).
     */
    public Form058PdfResponse toPdfResponse(Form058 form058, List<CardTableResponse> cards) {
        Patient patient = form058.getPatient();
        Form058ClinicalInfo clinicalInfo = form058.getClinicalInfo();
        Form058EpidemicInfo epidemicInfo = form058.getEpidemicInfo();
        Form058ReportInfo reportInfo = form058.getReportInfo();

        return new Form058PdfResponse(
                form058.getId(),
                form058.getUuid(),
                form058.getStatus(),
                organizationMappingHelper.activeOrganizationNameById(form058.getSenderOrganizationId()),
                organizationMappingHelper.activeOrganizationNameById(form058.getReceiverOrganizationId()),
                form058DetailResponseMapper.toResponse(form058.getDiagnosisInfo()),
                clinicalInfo == null ? null : clinicalInfo.getLabConfirmation(),
                toPatientResponse(patient),
                toAddressResponse(patient, AddressType.PERMANENT),
                toAddressResponse(patient, AddressType.TEMPORARY),
                toAffiliationResponse(patient, AffiliationType.WORKPLACE),
                toAffiliationResponse(patient, AffiliationType.EDUCATIONAL),
                form058DetailResponseMapper.toResponse(form058.getDateInfo()),
                organizationMappingHelper.activeOrganizationNameById(
                        clinicalInfo == null ? null : clinicalInfo.getHospitalPlaceId()
                ),
                epidemicInfo == null ? null : referenceMappingHelper.diseasePlaceName(epidemicInfo.getDiseasePlaceCode()),
                epidemicInfo == null ? null : epidemicInfo.getDiseaseCause(),
                epidemicInfo == null ? null : epidemicInfo.getEpidemicMeasures(),
                resolveNotifierFullName(form058, reportInfo),
                resolveReceiverFullName(cards),
                reportInfo == null ? null : reportInfo.getJournalFormCode(),
                reportInfo == null ? null : reportInfo.getComment(),
                form058DetailResponseMapper.toResponse(form058.getLocation())
        );
    }

    /**
     * The person who created the form is treated as the notifier ("Xabar beruvchi") - the
     * free-text {@code reportInfo.notifierFullName} the client submitted at creation time is
     * only a fallback for the rare case where {@code createdBy} isn't recorded (e.g. a
     * system-initiated record).
     */
    private String resolveNotifierFullName(Form058 form058, Form058ReportInfo reportInfo) {
        UserMiniResponse creator = userMapperHelper.toUserMiniResponse(form058.getCreatedBy());
        if (creator != null && StringUtils.hasText(creator.fullName())) {
            return creator.fullName();
        }
        return reportInfo == null ? null : reportInfo.getNotifierFullName();
    }

    /**
     * The person who attached a card to the form is treated as having received it ("Xabarni
     * qabul qilgan") - taken from whichever linked card was assigned first, since a single
     * assignCards call attaches every requested card type to the same assignee(s) at once.
     */
    private String resolveReceiverFullName(List<CardTableResponse> cards) {
        if (cards == null) {
            return null;
        }
        return cards.stream()
                .map(CardTableResponse::assignedBy)
                .filter(Objects::nonNull)
                .map(UserMiniResponse::fullName)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private Form058PdfPatientResponse toPatientResponse(Patient patient) {
        if (patient == null) {
            return null;
        }

        return new Form058PdfPatientResponse(
                patient.getId(),
                fullName(patient),
                referenceMappingHelper.genderName(patient.getGenderCode()),
                identifierValue(patient, IDENTIFIER_TYPES_PASSPORT),
                identifierValue(patient, IDENTIFIER_TYPES_PINFL),
                patient.getAgeYears(),
                patient.getAgeMonths(),
                patient.getBirthDate(),
                referenceMappingHelper.maritalStatusName(patient.getMaritalStatusCode()),
                referenceMappingHelper.professionName(patient.getProfessionCode()),
                patient.getPhoneNumber()
        );
    }

    private Form058PdfAddressResponse toAddressResponse(Patient patient, AddressType type) {
        PatientAddress address = findAddress(patient, type);
        if (address == null) {
            return null;
        }

        return new Form058PdfAddressResponse(
                referenceMappingHelper.regionName(address.getRegionCode()),
                referenceMappingHelper.districtName(address.getDistrictCode()),
                referenceMappingHelper.neighborhoodName(address.getNeighborhoodCode()),
                address.getStreetAddress(),
                address.getHouseNumber(),
                address.getApartmentNumber()
        );
    }

    /**
     * WORKPLACE and EDUCATIONAL are independent affiliation types (see {@link AffiliationType}) -
     * kept as two separate response fields rather than one ambiguous "workplace" so a child
     * with only a school affiliation, or a working student with both, is represented correctly.
     */
    private Form058PdfWorkplaceResponse toAffiliationResponse(Patient patient, AffiliationType type) {
        if (patient == null || patient.getAffiliations() == null) {
            return null;
        }

        return patient.getAffiliations().stream()
                .filter(affiliation -> affiliation.getType() == type)
                .findFirst()
                .map(affiliation -> new Form058PdfWorkplaceResponse(
                        affiliation.getOrganizationName(),
                        referenceMappingHelper.regionName(affiliation.getRegionCode()),
                        referenceMappingHelper.districtName(affiliation.getDistrictCode()),
                        affiliation.getAddress()
                ))
                .orElse(null);
    }

    private PatientAddress findAddress(Patient patient, AddressType type) {
        if (patient == null || patient.getAddresses() == null) {
            return null;
        }

        return patient.getAddresses().stream()
                .filter(address -> address.getType() == type)
                .findFirst()
                .orElse(null);
    }

    private String identifierValue(Patient patient, Set<String> acceptedTypeCodes) {
        if (patient.getIdentifiers() == null) {
            return null;
        }

        return patient.getIdentifiers().stream()
                .filter(identifier -> identifier.getTypeCode() != null
                        && acceptedTypeCodes.contains(identifier.getTypeCode().toUpperCase(Locale.ROOT)))
                .map(PatientIdentifier::getValue)
                .findFirst()
                .orElse(null);
    }

    private String fullName(Patient patient) {
        String joined = Stream.of(patient.getLastName(), patient.getFirstName(), patient.getMiddleName())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" "));
        return StringUtils.hasText(joined) ? joined : null;
    }
}
