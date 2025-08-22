package uz.uzinfocom.isemid.features.organization.services;

import org.springframework.data.domain.Page;
import uz.uzinfocom.isemid.features.organization.dtos.requests.OrgRequest;
import uz.uzinfocom.isemid.features.organization.models.Organization;
import uz.uzinfocom.isemid.features.organization.models.enums.MedicalType;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationService {
    Page<Organization> filter(int page, int size, String name, String stateCode, String cityCode, User user);

    Organization update(UUID uuid, OrgRequest orgRequest);

    Organization fetchOrganizationFromMisAndSave(UUID uuid, String tokenValue);

    Organization getById(long id);

    Organization updateUserFromMis(UUID uuid, User currentUser, String token);

    Optional<Organization> findById(UUID uuid);

    List<Organization> getChildrenByParentUuid(UUID uuid);

    List<User> getAllOrgUsers(long id);

    Optional<Organization> findById(long id);

    Optional<Organization> findByTin(String tin);

    List<Organization> getOrganizationsBySearch(String stateCode, String cityCode, MedicalType medicalType, String name);

    Organization save(Organization newOrg);

    List<Organization> getAllOrganizations();
}

