package uz.uzinfocom.app.platform.iam.application.user.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.exception.NotFoundException;
import uz.uzinfocom.app.platform.iam.application.organization.query.dto.OrganizationShortResponse;
import uz.uzinfocom.app.platform.iam.application.user.query.dto.UserOrganizationRoleAssignmentResponse;
import uz.uzinfocom.app.platform.iam.application.user.query.dto.UserOrganizationRolesResponse;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.domain.UserOrganizationRole;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.iam.repository.UserOrganizationRoleRepository;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserOrganizationRoleQueryService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final UserOrganizationRoleRepository userOrganizationRoleRepository;

    @Transactional(readOnly = true)
    public List<UserOrganizationRolesResponse> findOrganizationsWithRoles(Long userId) {
        User user = userRepository.findWithOrganizationsById(userId)
                .orElseThrow(() -> new NotFoundException("error.user.not_found", userId));

        return findOrganizationsWithRoles(user, null);
    }

    @Transactional(readOnly = true)
    public List<UserOrganizationRolesResponse> findOrganizationsWithRoles(User user, UUID selectedOrganizationUuid) {
        List<UserOrganizationRole> assignments =
                userOrganizationRoleRepository.findByUserIdAndActiveTrueAndDeletedAtIsNullOrderByOrganizationNameAscRoleNameAsc(
                        user.getId()
                );

        Map<Long, List<Role>> rolesByOrganization = assignments.stream()
                .filter(UserOrganizationRole::isActiveAssignment)
                .filter(assignment -> assignment.getOrganization() != null)
                .filter(assignment -> assignment.getRole() != null && assignment.getRole().isAvailableForAuthorization())
                .collect(Collectors.groupingBy(
                        assignment -> assignment.getOrganization().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(UserOrganizationRole::getRole, Collectors.toList())
                ));

        Map<Long, Organization> organizationsById = new LinkedHashMap<>();

        if (user.getOrganizations() != null) {
            user.getOrganizations().stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(Organization::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                    .forEach(organization -> organizationsById.put(organization.getId(), organization));
        }

        assignments.stream()
                .map(UserOrganizationRole::getOrganization)
                .filter(Objects::nonNull)
                .forEach(organization -> organizationsById.putIfAbsent(organization.getId(), organization));

//        return organizationsById.values().stream()
//                .map(organization -> toOrganizationRolesResponse(
//                        organization,
//                        selectedOrganizationUuid,
//                        rolesByOrganization.getOrDefault(organization.getId(), List.of())
//                ))
//                .toList();

        return null;
    }

    @Transactional(readOnly = true)
    public UserOrganizationRoleAssignmentResponse findUserRolesByOrganization(Long userId, Long organizationId) {
        User user = userRepository.findWithOrganizationsById(userId)
                .orElseThrow(() -> new NotFoundException("error.user.not_found", userId));

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new NotFoundException("organization.not.found", organizationId));

        List<Role> roles = activeRoles(userId, organizationId);
        return null;
    }

    @Transactional(readOnly = true)
    public List<Role> activeRoles(Long userId, Long organizationId) {
        return userOrganizationRoleRepository.findActiveRolesWithPermissions(userId, organizationId)
                .stream()
                .filter(Objects::nonNull)
                .filter(Role::isAvailableForAuthorization)
                .sorted(Comparator.comparing(Role::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private OrganizationShortResponse toShortOrganization(Organization organization) {
        return new OrganizationShortResponse(
                organization.getId(),
                organization.getUuid(),
                organization.getName()
        );
    }
}
