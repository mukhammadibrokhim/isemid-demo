package uz.uzinfocom.isemid.services.impls;

import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.models.entities.Organization;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.enums.AccessLevel;
import uz.uzinfocom.isemid.models.entities.enums.CatalogType;
import uz.uzinfocom.isemid.models.payload.requests.ReqOrganization;
import uz.uzinfocom.isemid.models.payload.responses.RespOrganization;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.OrganizationRepository;
import uz.uzinfocom.isemid.services.OrganizationService;
import uz.uzinfocom.isemid.services.value_set.CatalogService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StringUtils.hasText;
import static uz.uzinfocom.isemid.utils.CommonUtils.*;
import static uz.uzinfocom.isemid.utils.IntegrationUrlConstraint.MIS_ORGANIZATION;
import static uz.uzinfocom.isemid.utils.RestTemplateUtil.makeRequest;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
    final OrganizationRepository organizationRepository;
    final CatalogService catalogService;

    @Override
    public ApiResponsePage getAllOrganizationsByFilter(int page, int size, String name, String stateCode, String cityCode, HttpServletRequest request) {
        Specification<Organization> spec = getFilteredSpecification(name, stateCode, cityCode);
        Page<Organization> organizationPage = organizationRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, organizationPage, getFullURL(request));
    }

    @Override
    public Organization fetchOrganizationFromMisAndSave(UUID uuid, String tokenValue) {
        RespOrganization response = makeRequest(String.format(MIS_ORGANIZATION, uuid), HttpMethod.GET, tokenValue, null, RespOrganization.class);
        return organizationRepository.save(mappingOrganization(response));
    }

    @Override
    public ApiResponse<Organization> getById(long id) {
        return new ApiResponse<>(true, "Organization successfully fetch.", organizationRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Organization not found.")));
    }

    @Override
    public ApiResponse<Organization> updateUserFromMis(UUID uuid, User currentUser, String token) {
        RespOrganization response = makeRequest(String.format(MIS_ORGANIZATION, uuid), HttpMethod.GET, token, null, RespOrganization.class);
        return new ApiResponse<>(true, "Organization successfully updated.", mappingOrganization(response));
    }

    @Override
    public Optional<Organization> findById(UUID uuid) {
        return organizationRepository.findByUuid(uuid);
    }

    @Override
    public ApiResponse<Organization> update(UUID uuid, ReqOrganization reqOrganization, User currentUser) {
        Organization existingOrg = organizationRepository.findByUuid(uuid).orElseThrow(() -> new ApiException(NOT_FOUND, "Organization not found."));
        Optional.ofNullable(reqOrganization.getName()).ifPresent(existingOrg::setName);
        Optional.of(reqOrganization.isActive()).ifPresent(existingOrg::setActive);
        Optional.ofNullable(reqOrganization.getTin()).ifPresent(existingOrg::setTin);
        Optional.ofNullable(reqOrganization.getEmail()).ifPresent(existingOrg::setEmail);
        Optional.ofNullable(reqOrganization.getPhone()).ifPresent(existingOrg::setPhone);
        Optional.ofNullable(reqOrganization.getLevelCode())
                .map(code -> catalogService.findByTypeAndCode(CatalogType.level, code))
                .ifPresent(code -> existingOrg.setCountry(code.orElse(null)));

        Optional.ofNullable(reqOrganization.getType()).ifPresent(existingOrg::setType);
        Optional.ofNullable(reqOrganization.getDistrict()).ifPresent(existingOrg::setDistrict);
        Optional.ofNullable(reqOrganization.getLine()).ifPresent(existingOrg::setLine);
        Optional.ofNullable(reqOrganization.getServiceAreaCode())
                .map(code -> catalogService.findByTypeAndCode(CatalogType.state, code))
                .ifPresent(code -> existingOrg.setCountry(code.orElse(null)));

        Optional.ofNullable(reqOrganization.getCountryCode())
                .map(code -> catalogService.findByTypeAndCode(CatalogType.country, code))
                .ifPresent(code -> existingOrg.setCountry(code.orElse(null)));

        Optional.ofNullable(reqOrganization.getStateCode())
                .map(code -> catalogService.findByTypeAndCode(CatalogType.state, code))
                .ifPresent(code -> existingOrg.setCountry(code.orElse(null)));

        Optional.ofNullable(reqOrganization.getCityCode())
                .map(code -> catalogService.findByTypeAndCode(CatalogType.city, code))
                .ifPresent(code -> existingOrg.setCountry(code.orElse(null)));

        if (reqOrganization.getParentUuid() != null) {
            Organization parentOrg = organizationRepository.findByUuid(reqOrganization.getParentUuid()).orElseThrow(() -> new ApiException(NOT_FOUND, "Organization not found."));
            parentOrg.getChildren().add(existingOrg);
            existingOrg.setParent(parentOrg);
            existingOrg.setParentId(parentOrg.getId());
        } else {
            existingOrg.setParent(null);
        }

        return new ApiResponse<>(true, "Success", organizationRepository.save(existingOrg));
    }

    @Override
    public ApiResponse<List<Organization>> getChildrenByParentUuid(UUID uuid) {
        Organization organization = organizationRepository.findByUuid(uuid).orElseThrow(() -> new ApiException(NOT_FOUND, "Organization not found."));
        return new ApiResponse<>(true, "success", organization.getChildren());
    }

    @Override
    public ApiResponse<?> getAllOrgUsers(long id) {
        Organization organization = organizationRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Organization not found."));
        return new ApiResponse<>(true, "success", organization.getUsers());
    }

    @Override
    public Optional<Organization> findById(long id) {
        return organizationRepository.findById(id);
    }

    @Override
    public Optional<Organization> findByTin(String tin) {
        return organizationRepository.findByTin(tin);
    }

    @Override
    public ApiResponse<?> getOrganizationsBySearch(String stateCode, String cityCode, String name) {
        return new ApiResponse<>(true, "Success", organizationRepository.searchByFilter(stateCode, cityCode, name));
    }

    @Override
    public Organization save(Organization newOrg) {
        return organizationRepository.save(newOrg);
    }

    private Organization mappingOrganization(RespOrganization response) {
        Organization organization = new Organization();
        organization.setUuid(UUID.fromString(response.getId()));
        organization.setName(response.getName());
        organization.setActive(response.isActive());

        if (response.getTelecom() != null) {
            response.getTelecom().forEach(telecom -> {
                if ("email".equalsIgnoreCase(telecom.getSystem())) {
                    organization.setEmail(telecom.getValue());
                } else if ("phone".equalsIgnoreCase(telecom.getSystem())) {
                    organization.setPhone(telecom.getValue());
                }
            });
        }

        if (response.getAddress() != null && !response.getAddress().isEmpty()) {
            RespOrganization.Address address = response.getAddress().stream()
                    .filter(addr -> "both".equalsIgnoreCase(addr.getType()))
                    .findFirst()
                    .orElse(response.getAddress().getFirst());

            organization.setCountry(catalogService.findByTypeAndCode(CatalogType.country, address.getCountry()).orElse(null));
            organization.setState(catalogService.findByTypeAndCode(CatalogType.state, address.getState()).orElse(null));
            organization.setCity(catalogService.findByTypeAndCode(CatalogType.city, address.getCity()).orElse(null));
            organization.setLine(address.getLine());
            organization.setType(address.getType());
            organization.setDistrict(address.getDistrict());
        }
        if (!response.getType().isEmpty()) {
            response.getType().forEach(type -> type.getCoding()
                    .stream().filter(coding -> "http://fhir.ssv.uz/ValueSet/organization-type-level".equals(coding.getSystem()))
                    .forEach(coding -> {
                        switch (coding.getCode()) {
                            case "1":
                                organization.setLevel(catalogService.findByTypeAndCode(CatalogType.level, AccessLevel.republic_level.name()).orElse(null));
                                break;
                            case "2":
                                organization.setLevel(catalogService.findByTypeAndCode(CatalogType.level, AccessLevel.state_level.name()).orElse(null));
                                break;
                            case "3":
                            case "4":
                                organization.setLevel(catalogService.findByTypeAndCode(CatalogType.level, AccessLevel.district_level.name()).orElse(null));
                                break;
                            default:
                                organization.setLevel(null);
                        }
                    }));
        }

        if (response.getServiceArea() != null && !response.getServiceArea().isEmpty()) {
            organization.setServiceArea(catalogService.findByTypeAndCode(CatalogType.state, response.getServiceArea().getFirst().getState()).orElse(null));
        }

        if (response.getIdentifier() != null) {
            response.getIdentifier().stream()
                    .filter(identifier -> identifier.getType().getCoding().stream()
                            .anyMatch(coding -> "TAX".equalsIgnoreCase(coding.getCode())))
                    .findFirst()
                    .ifPresent(identifier -> organization.setTin(identifier.getValue()));
        }

        return organization;
    }

    public static Specification<Organization> getFilteredSpecification(
            String name,
            String stateCode,
            String cityCode
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isNull(root.get("parent")));

            if (hasText(name)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (hasText(stateCode)) {
                predicates.add(criteriaBuilder.equal(root.get("state").get("code"), stateCode));
            }
            if (hasText(cityCode)) {
                predicates.add(criteriaBuilder.equal(root.get("city").get("code"), cityCode));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
