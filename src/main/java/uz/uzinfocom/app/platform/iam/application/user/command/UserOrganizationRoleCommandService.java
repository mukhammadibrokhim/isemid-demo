package uz.uzinfocom.app.platform.iam.application.user.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.exception.ConflictException;
import uz.uzinfocom.app.platform.exception.NotFoundException;
import uz.uzinfocom.app.platform.iam.application.user.query.UserOrganizationRoleQueryService;
import uz.uzinfocom.app.platform.iam.application.user.query.dto.UserOrganizationRoleAssignmentResponse;
import uz.uzinfocom.app.platform.iam.application.user.query.dto.UserOrganizationRolesResponse;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.iam.domain.UserOrganizationRole;
import uz.uzinfocom.app.platform.iam.repository.OrganizationRepository;
import uz.uzinfocom.app.platform.iam.repository.RoleRepository;
import uz.uzinfocom.app.platform.iam.repository.UserOrganizationRoleRepository;
import uz.uzinfocom.app.platform.iam.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserOrganizationRoleCommandService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final UserOrganizationRoleRepository userOrganizationRoleRepository;
    private final UserOrganizationRoleQueryService queryService;

    @Transactional
    public List<UserOrganizationRolesResponse> assignUserToOrganization(Long userId, Long organizationId) {
        User user = getUserWithOrganizations(userId);
        Organization organization = getOrganization(organizationId);

        ensureMembership(user, organization);
        userRepository.save(user);

        return queryService.findOrganizationsWithRoles(user.getId());
    }

    @Transactional
    public List<UserOrganizationRolesResponse> removeUserFromOrganization(Long userId, Long organizationId) {
        User user = getUserWithOrganizations(userId);
        Organization organization = getOrganization(organizationId);

        userOrganizationRoleRepository
                .findByUserIdAndOrganizationIdAndActiveTrueAndDeletedAtIsNullOrderByRoleNameAsc(userId, organizationId)
                .forEach(UserOrganizationRole::deactivate);

        if (user.getOrganizations() != null) {
            user.getOrganizations().removeIf(existing -> Objects.equals(existing.getId(), organization.getId()));
        }

        userRepository.save(user);
        return queryService.findOrganizationsWithRoles(user.getId());
    }

    @Transactional
    public UserOrganizationRoleAssignmentResponse addRoles(Long userId, Long organizationId, Collection<Long> roleIds) {
        User user = getUserWithOrganizations(userId);
        Organization organization = getOrganization(organizationId);
        List<Role> roles = getAvailableRoles(roleIds);

        ensureMembership(user, organization);

        for (Role role : roles) {
            upsertAssignment(user, organization, role);
        }

        userRepository.save(user);
//        return queryService.toAssignmentResponse(user, organization, queryService.activeRoles(userId, organizationId));
        return null;
    }

    @Transactional
    public UserOrganizationRoleAssignmentResponse replaceRoles(Long userId, Long organizationId,
                                                               Collection<Long> roleIds) {
        User user = getUserWithOrganizations(userId);
        Organization organization = getOrganization(organizationId);
        List<Role> requestedRoles = getAvailableRoles(roleIds);

        ensureMembership(user, organization);

        Set<Long> requestedRoleIds = requestedRoles.stream()
                .map(Role::getId)
                .collect(Collectors.toUnmodifiableSet());

        userOrganizationRoleRepository
                .findByUserIdAndOrganizationIdAndActiveTrueAndDeletedAtIsNullOrderByRoleNameAsc(userId, organizationId)
                .stream()
                .filter(assignment -> !requestedRoleIds.contains(assignment.getRole().getId()))
                .forEach(UserOrganizationRole::deactivate);

        for (Role role : requestedRoles) {
            upsertAssignment(user, organization, role);
        }

        userRepository.save(user);
//        return queryService.toAssignmentResponse(user, organization, queryService.activeRoles(userId, organizationId));

        return null;
    }

    @Transactional
    public UserOrganizationRoleAssignmentResponse removeRole(Long userId, Long organizationId, Long roleId) {
        User user = getUserWithOrganizations(userId);
        Organization organization = getOrganization(organizationId);

        UserOrganizationRole assignment = userOrganizationRoleRepository
                .findByUserIdAndOrganizationIdAndRoleIdAndActiveTrueAndDeletedAtIsNull(userId, organizationId, roleId)
                .orElseThrow(() -> new NotFoundException("user.organization.role.not.found", userId, organizationId, roleId));

        assignment.deactivate();

//        return queryService.toAssignmentResponse(user, organization, queryService.activeRoles(userId, organizationId));

        return null;
    }

    private void upsertAssignment(User user, Organization organization, Role role) {
        Optional<UserOrganizationRole> existing = userOrganizationRoleRepository
                .findTopByUserIdAndOrganizationIdAndRoleIdOrderByIdDesc(
                        user.getId(),
                        organization.getId(),
                        role.getId()
                );

        if (existing.isPresent()) {
            UserOrganizationRole assignment = existing.get();
            if (assignment.isActiveAssignment()) {
                return;
            }
            assignment.restore();
            return;
        }

        UserOrganizationRole assignment = UserOrganizationRole.builder()
                .user(user)
                .organization(organization)
                .role(role)
                .active(true)
                .build();

        userOrganizationRoleRepository.save(assignment);
    }

    private User getUserWithOrganizations(Long userId) {
        return userRepository.findWithOrganizationsById(userId)
                .orElseThrow(() -> new NotFoundException("error.user.not_found", userId));
    }

    private Organization getOrganization(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new NotFoundException("organization.not.found", organizationId));

        if (Boolean.FALSE.equals(organization.getActive())) {
            throw new ConflictException("organization.not.active", organizationId);
        }

        return organization;
    }

    private List<Role> getAvailableRoles(Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new ConflictException("role.ids.required");
        }

        LinkedHashSet<Long> normalizedRoleIds = roleIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Role> roles = roleRepository.findAllById(normalizedRoleIds);

        if (roles.size() != normalizedRoleIds.size()) {
            throw new NotFoundException("role.not.found");
        }

        roles.forEach(role -> {
            if (!role.isAvailableForAuthorization()) {
                throw new ConflictException("role.not.available", role.getId());
            }
        });

        return roles.stream()
                .sorted(Comparator.comparing(Role::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private void ensureMembership(User user, Organization organization) {
        if (user.getOrganizations() == null) {
            user.setOrganizations(new LinkedHashSet<>());
        }

        boolean alreadyMember = user.getOrganizations().stream()
                .filter(Objects::nonNull)
                .anyMatch(existing -> Objects.equals(existing.getId(), organization.getId()));

        if (!alreadyMember) {
            user.getOrganizations().add(organization);
        }
    }
}
