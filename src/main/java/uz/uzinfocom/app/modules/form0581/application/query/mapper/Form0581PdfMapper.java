package uz.uzinfocom.app.modules.form0581.application.query.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form0581.application.query.dto.pdf.Form0581PdfAddressResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.pdf.Form0581PdfAnimalOwnerResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.pdf.Form0581PdfAnimalResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.pdf.Form0581PdfHospitalizationResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.pdf.Form0581PdfIncidentResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.pdf.Form0581PdfLocationResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.pdf.Form0581PdfOtherInjuredPersonResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.pdf.Form0581PdfPatientResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.pdf.Form0581PdfResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.pdf.Form0581PdfWorkplaceResponse;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581OtherInjuredPerson;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581Address;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581AnimalInfo;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581AnimalOwnerInfo;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581HospitalizationInfo;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581IncidentInfo;
import uz.uzinfocom.app.modules.form0581.domain.model.embedded.Form0581ReportInfo;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builds the print-oriented view of a Form0581: every coded field (region, gender,
 * marital status, profession, animal category) is resolved to its display name here,
 * unlike {@link Form0581DetailResponseMapper} which returns raw catalog codes for API
 * consumers that already have their own catalog cache. Mirrors {@code Form058PdfMapper}
 * field-for-field where the two forms share shape, but is not code-shared with it - see
 * {@link Form0581} for why form058/form0581 are deliberately independent siblings.
 */
@Component
@RequiredArgsConstructor
public class Form0581PdfMapper {

    /**
     * Two identifier type-code conventions coexist in the real data (PINFL/PASSPORT is the
     * dominant one, NNUZB/PPN a minority) - both are matched so the PDF resolves either way.
     */
    private static final Set<String> IDENTIFIER_TYPES_PINFL = Set.of("PINFL", "NNUZB");
    private static final Set<String> IDENTIFIER_TYPES_PASSPORT = Set.of("PASSPORT", "PPN");

    private final Form0581DetailResponseMapper form0581DetailResponseMapper;
    private final ReferenceMappingHelper referenceMappingHelper;
    private final OrganizationMappingHelper organizationMappingHelper;
    private final UserMapperHelper userMapperHelper;

    public Form0581PdfResponse toPdfResponse(Form0581 form0581) {
        Patient patient = form0581.getPatient();
        Form0581HospitalizationInfo hospitalizationInfo = form0581.getHospitalizationInfo();
        Form0581ReportInfo reportInfo = form0581.getReportInfo();

        return new Form0581PdfResponse(
                form0581.getId(),
                form0581.getUuid(),
                form0581.getStatus(),
                organizationMappingHelper.activeOrganizationNameById(form0581.getSenderOrganizationId()),
                organizationMappingHelper.activeOrganizationNameById(form0581.getReceiverOrganizationId()),
                form0581DetailResponseMapper.toResponse(form0581.getDiagnosisInfo()),
                toPatientResponse(patient),
                toAddressResponse(patient, AddressType.PERMANENT),
                toAddressResponse(patient, AddressType.TEMPORARY),
                toAffiliationResponse(patient, AffiliationType.WORKPLACE),
                toAffiliationResponse(patient, AffiliationType.EDUCATIONAL),
                toIncidentResponse(form0581.getIncidentInfo()),
                toAnimalResponse(form0581.getAnimalInfo()),
                toAnimalOwnerResponse(form0581.getAnimalOwnerInfo()),
                form0581.getOtherPeopleInjured(),
                toOtherInjuredPeopleResponse(form0581.getOtherInjuredPeople()),
                toHospitalizationResponse(hospitalizationInfo),
                reportInfo == null ? null : reportInfo.getAntirabicAssistanceInfo(),
                resolveNotifierFullName(form0581, reportInfo),
                reportInfo == null ? null : reportInfo.getReceiverFullName(),
                reportInfo == null ? null : reportInfo.getMessageSentAt()
        );
    }

    /**
     * The person who created the form is treated as the notifier ("Xabar beruvchi") - the
     * free-text {@code reportInfo.notifierFullName} the client submitted at creation time is
     * only a fallback for the rare case where {@code createdBy} isn't recorded (e.g. a
     * system-initiated record). Mirrors {@code Form058PdfMapper#resolveNotifierFullName}.
     */
    private String resolveNotifierFullName(Form0581 form0581, Form0581ReportInfo reportInfo) {
        UserMiniResponse creator = userMapperHelper.toUserMiniResponse(form0581.getCreatedBy());
        if (creator != null && StringUtils.hasText(creator.fullName())) {
            return creator.fullName();
        }
        return reportInfo == null ? null : reportInfo.getNotifierFullName();
    }

    private Form0581PdfPatientResponse toPatientResponse(Patient patient) {
        if (patient == null) {
            return null;
        }

        return new Form0581PdfPatientResponse(
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

    private Form0581PdfAddressResponse toAddressResponse(Patient patient, AddressType type) {
        PatientAddress address = findAddress(patient, type);
        if (address == null) {
            return null;
        }

        return new Form0581PdfAddressResponse(
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
    private Form0581PdfWorkplaceResponse toAffiliationResponse(Patient patient, AffiliationType type) {
        if (patient == null || patient.getAffiliations() == null) {
            return null;
        }

        return patient.getAffiliations().stream()
                .filter(affiliation -> affiliation.getType() == type)
                .findFirst()
                .map(affiliation -> new Form0581PdfWorkplaceResponse(
                        affiliation.getOrganizationName(),
                        referenceMappingHelper.regionName(affiliation.getRegionCode()),
                        referenceMappingHelper.districtName(affiliation.getDistrictCode()),
                        affiliation.getAddress()
                ))
                .orElse(null);
    }

    private Form0581PdfIncidentResponse toIncidentResponse(Form0581IncidentInfo incidentInfo) {
        if (incidentInfo == null) {
            return null;
        }

        return new Form0581PdfIncidentResponse(
                incidentInfo.getInjuryDateTime(),
                incidentInfo.getDpuVisitDateTime(),
                referenceMappingHelper.regionName(incidentInfo.getInjuryRegionCode()),
                referenceMappingHelper.districtName(incidentInfo.getInjuryDistrictCode()),
                incidentInfo.getInjuryAddress()
        );
    }

    private Form0581PdfAnimalResponse toAnimalResponse(Form0581AnimalInfo animalInfo) {
        if (animalInfo == null) {
            return null;
        }

        return new Form0581PdfAnimalResponse(
                referenceMappingHelper.animalCategoryName(animalInfo.getAnimalCategoryCode()),
                animalInfo.getAnimalColor(),
                animalInfo.getAnimalType(),
                animalInfo.getAnimalBreed()
        );
    }

    private Form0581PdfAnimalOwnerResponse toAnimalOwnerResponse(Form0581AnimalOwnerInfo ownerInfo) {
        if (ownerInfo == null) {
            return null;
        }

        String fullName = fullName(ownerInfo.getOwnerLastName(), ownerInfo.getOwnerFirstName(), ownerInfo.getOwnerMiddleName());
        if (fullName == null && ownerInfo.getOwnerAddress() == null) {
            return null;
        }

        return new Form0581PdfAnimalOwnerResponse(fullName, toLocationResponse(ownerInfo.getOwnerAddress()));
    }

    private List<Form0581PdfOtherInjuredPersonResponse> toOtherInjuredPeopleResponse(
            List<Form0581OtherInjuredPerson> otherInjuredPeople
    ) {
        if (otherInjuredPeople == null) {
            return List.of();
        }

        return otherInjuredPeople.stream()
                .map(person -> new Form0581PdfOtherInjuredPersonResponse(
                        fullName(person.getLastName(), person.getFirstName(), person.getMiddleName()),
                        toLocationResponse(person.getAddress())
                ))
                .toList();
    }

    private Form0581PdfLocationResponse toLocationResponse(Form0581Address address) {
        if (address == null) {
            return null;
        }

        return new Form0581PdfLocationResponse(
                referenceMappingHelper.regionName(address.getRegionCode()),
                referenceMappingHelper.districtName(address.getDistrictCode()),
                referenceMappingHelper.neighborhoodName(address.getNeighborhoodCode()),
                address.getStreet(),
                address.getHouseNumber(),
                address.getApartmentNumber()
        );
    }

    private Form0581PdfHospitalizationResponse toHospitalizationResponse(Form0581HospitalizationInfo hospitalizationInfo) {
        if (hospitalizationInfo == null) {
            return null;
        }

        return new Form0581PdfHospitalizationResponse(
                hospitalizationInfo.getHospitalizedAt(),
                organizationMappingHelper.activeOrganizationNameById(hospitalizationInfo.getHospitalOrganizationId())
        );
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
        return fullName(patient.getLastName(), patient.getFirstName(), patient.getMiddleName());
    }

    private String fullName(String lastName, String firstName, String middleName) {
        String joined = Stream.of(lastName, firstName, middleName)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" "));
        return StringUtils.hasText(joined) ? joined : null;
    }
}
