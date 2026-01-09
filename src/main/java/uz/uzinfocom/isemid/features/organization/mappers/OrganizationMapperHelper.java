package uz.uzinfocom.isemid.features.organization.mappers;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uz.uzinfocom.isemid.commons.exceptions.ApiException;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.organization.dtos.responses.MisOrganizationResponse;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgChildResponse;
import uz.uzinfocom.isemid.features.organization.dtos.responses.OrgMiniResponse;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.LevelType;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.organization.models.enums.ServiceType;
import uz.uzinfocom.isemid.features.organization.reposiotries.OrganizationRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
@RequiredArgsConstructor
public class OrganizationMapperHelper {
    private final OrganizationRepository organizationRepository;
    private final MessageUtil messageUtil;

    @Named("uuidToLongId")
    public Long uuidToLongId(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return organizationRepository.findByUuid(uuid)
                .map(Organization::getId)
                .orElseThrow(() -> new ApiException(NOT_FOUND, messageUtil.get("error.not_found_by_id", uuid)));
    }

    @Named("mapChildren")
    public List<OrgChildResponse> mapChildren(List<Organization> children) {
        if (children == null) {
            return null;
        }
        return children.stream().map(org ->
                OrgChildResponse.builder()
                        .id(org.getId())
                        .uuid(org.getUuid())
                        .name(org.getName())
                        .parentId(org.getParentId())
                        .children(mapChildren(org.getChildren()))
                        .build()
        ).collect(Collectors.toList());
    }

    @Named("toOrgMiniResponse")
    public OrgMiniResponse toOrgMiniResponse(UUID uuid) {
        if (uuid == null) return null;
        Organization organization = organizationRepository.findByUuid(uuid).orElse(null);
        if (organization == null) {
            return null;
        }
        return OrgMiniResponse.builder()
                .id(organization.getId())
                .uuid(organization.getUuid())
                .name(organization.getName())
                .build();
    }

    @Named("toOrgMiniResponse")
    public OrgMiniResponse toOrgMiniResponse(Long id) {
        if (id == null) return null;

        Organization organization = organizationRepository.findById(id).orElse(null);
        if (organization == null) {
            return null;
        }
        return OrgMiniResponse.builder()
                .id(organization.getId())
                .uuid(organization.getUuid())
                .name(organization.getName())
                .build();
    }

    @Named("extractEmail")
    public String extractEmail(MisOrganizationResponse organizationResponse) {
        if (organizationResponse.getTelecom() == null) {
            return null;
        }

        return organizationResponse.getTelecom().stream()
                .filter(t -> "email".equalsIgnoreCase(t.getSystem()))
                .map(MisOrganizationResponse.Telecom::getValue)
                .findFirst()
                .orElse(null);
    }

    @Named("extractPhone")
    public String extractPhone(MisOrganizationResponse organizationResponse) {
        if (organizationResponse.getTelecom() == null) {
            return null;
        }
        return organizationResponse.getTelecom().stream()
                .filter(t -> "phone".equalsIgnoreCase(t.getSystem()))
                .map(MisOrganizationResponse.Telecom::getValue)
                .findFirst().orElse(null);
    }

    @Named("extractServiceTypes")
    public List<ServiceType> extractServiceTypes(MisOrganizationResponse response) {
        return extractTypesBySystem(response, "http://fhir.ssv.uz/ValueSet/organization-type-service").stream()
                .map(ServiceType::fromCode)
                .filter(Objects::nonNull)
                .toList();
    }

    @Named("extractMedicalType")
    public MedicalType extractMedicalType(MisOrganizationResponse response) {
        return extractTypesBySystem(response, "http://fhir.ssv.uz/ValueSet/organization-type-medical").stream()
                .map(MedicalType::fromCode)
                .findFirst() // faqat birinchi mos kelganini oladi
                .orElse(null); // agar topilmasa null
    }

    @Named("extractLevelType")
    public LevelType extractLevelType(MisOrganizationResponse response) {
        return extractTypesBySystem(response, "http://fhir.ssv.uz/ValueSet/organization-type-level").stream()
                .map(LevelType::fromCode)
                .findFirst()
                .orElse(null);
    }


    public List<String> extractTypesBySystem(MisOrganizationResponse response, String system) {
        if (response.getType() == null) return List.of();

        return response.getType().stream()
                .filter(Objects::nonNull)
                .flatMap(t -> t.getCoding().stream())
                .filter(coding -> coding != null && system.equals(coding.getSystem()))
                .map(MisOrganizationResponse.Coding::getCode)
                .filter(Objects::nonNull)
                .toList();
    }

    @Named("extractCountryCode")
    public String extractCountryCode(MisOrganizationResponse organizationResponse) {
        if (organizationResponse.getAddress() == null) {
            return null;
        }
        return organizationResponse.getAddress().stream()
                .map(MisOrganizationResponse.Address::getCountry)
                .findFirst().orElse(null);
    }

    @Named("extractStateCode")
    public String extractStateCode(MisOrganizationResponse organizationResponse) {
        if (organizationResponse.getAddress() == null) {
            return null;
        }
        return organizationResponse.getAddress().stream()
                .map(MisOrganizationResponse.Address::getState)
                .findFirst().orElse(null);
    }

    @Named("extractCityCode")
    public String extractCityCode(MisOrganizationResponse organizationResponse) {
        if (organizationResponse.getAddress() == null) {
            return null;
        }
        return organizationResponse.getAddress().stream()
                .map(MisOrganizationResponse.Address::getCity)
                .findFirst().orElse(null);
    }

    @Named("extractDistrict")
    public String extractDistrict(MisOrganizationResponse organizationResponse) {
        if (organizationResponse.getAddress() == null) {
            return null;
        }

        return organizationResponse.getAddress().stream()
                .filter(Objects::nonNull)
                .map(MisOrganizationResponse.Address::getDistrict)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Named("extractLine")
    public String extractLine(MisOrganizationResponse organizationResponse) {
        if (organizationResponse.getAddress() == null) {
            return null;
        }
        return organizationResponse.getAddress().stream()
                .map(MisOrganizationResponse.Address::getLine)
                .findFirst().orElse(null);
    }

    @Named("extractServiceArea")
    public String extractServiceArea(MisOrganizationResponse organizationResponse) {
        if (organizationResponse.getServiceArea() != null && !organizationResponse.getServiceArea().isEmpty()) {
            return organizationResponse.getServiceArea().stream()
                    .map(MisOrganizationResponse.ServiceArea::getState)
                    .findFirst().orElse(null);
        }
        return null;
    }

    @Named("extractTin")
    public String extractTin(MisOrganizationResponse organizationResponse) {
        if (organizationResponse.getIdentifier() == null || organizationResponse.getIdentifier().isEmpty()) {
            return null;
        }

        return organizationResponse.getIdentifier().stream()
                .filter(identifier -> identifier.getType() != null &&
                        identifier.getType().getCoding().stream()
                                .anyMatch(coding -> "TAX".equalsIgnoreCase(coding.getCode())))
                .map(MisOrganizationResponse.Identifier::getValue)
                .findFirst()
                .orElse(null);
    }

}
