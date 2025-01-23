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
import uz.uzinfocom.isemid.models.payload.responses.RespOrganization;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.repositories.OrganizationRepository;
import uz.uzinfocom.isemid.services.OrganizationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uz.uzinfocom.isemid.utils.CommonUtils.*;
import static uz.uzinfocom.isemid.utils.IntegrationUrlConstraint.MIS_ORGANIZATION;
import static uz.uzinfocom.isemid.utils.RestTemplateUtil.makeRequest;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
    final OrganizationRepository organizationRepository;

    @Override
    public ApiResponsePage getAllOrganizationsByFilter(int page, int size, HttpServletRequest request) {
        Specification<Organization> spec = getFilteredSpecification();
        Page<Organization> organizationPage = organizationRepository.findAll(spec, pageableSortByIdDesc(page, size));
        return createApiResponsePage(page, organizationPage, getFullURL(request));
    }

    @Override
    public Organization fetchOrganizationFromMisAndSave(UUID uuid, String tokenValue) {
        RespOrganization response = makeRequest(String.format(MIS_ORGANIZATION, uuid), HttpMethod.GET, tokenValue, null, RespOrganization.class);
        return organizationRepository.save(mappingOrganization(response));
    }

    @Override
    public Optional<Organization> findByUuid(UUID uuid) {
        return organizationRepository.findByUuid(uuid);
    }

    @Override
    public ApiResponse<Organization> getById(long id) {
        return new ApiResponse<>(true, "Organization successfully fetch.", organizationRepository.findById(id).orElseThrow(() -> new ApiException(NOT_FOUND, "Organization not found.")));
    }

    @Override
    public ApiResponse<Organization> updateUserFromMis(UUID uuid, User currentUser, String token) {
        return new ApiResponse<>(true, "Organization successfully updated.", null);
    }

    @Override
    public Optional<Organization> findById(UUID uuid) {
        return organizationRepository.findByUuid(uuid);
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

            organization.setCountry(address.getCountry());
            organization.setState(address.getState());
            organization.setCity(address.getCity());
            organization.setLine(address.getLine());
            organization.setType(address.getType());
            organization.setDistrict(address.getDistrict());
        }

        if (response.getServiceArea() != null && !response.getServiceArea().isEmpty()) {
            organization.setServiceArea(response.getServiceArea().getFirst().getState());
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
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
