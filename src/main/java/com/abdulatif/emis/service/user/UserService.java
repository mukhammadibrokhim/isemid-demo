package com.abdulatif.emis.service.user;


import com.abdulatif.emis.dto.user.UserResponseDTO;
import com.abdulatif.emis.exception.RecordForbiddenException;
import com.abdulatif.emis.exception.RecordNotFoundException;
import com.abdulatif.emis.model.organization.Organization;
import com.abdulatif.emis.model.user.User;
import com.abdulatif.emis.model.enums.Status;
import com.abdulatif.emis.model.util.*;
import com.abdulatif.emis.repository.organization.OrganizationRepository;
import com.abdulatif.emis.repository.region.DistrictRepository;
import com.abdulatif.emis.repository.region.RegionRepository;
import com.abdulatif.emis.repository.user.RoleRepository;
import com.abdulatif.emis.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final DistrictRepository districtRepository;
    private final RegionRepository regionRepository;

    public User findByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) throw new RecordForbiddenException("User with username: " + username + " not found");
        log.info("IN findByUsername - user: {} found by username: {}", user, username);
        return user;
    }

    public User currentUser() {
        return userRepository.findByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
    }

    public String currentUserFullName() {
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null)
            return user.getFirstName() + " " + user.getLastName() + " " + user.getMiddleName();
        return null;
    }

    public List<Status> getStatusesByUserRole() {
        User user = currentUser();
        List<Status> statuses = new ArrayList<>();
//        if (user.getRoles().contains(roleRepository.findByName("lis_registrar"))) {
//            statuses.addAll(List.of(Status.ACTIVE, Status.REGISTERED, Status.NURSE, Status.LABORATORY, Status.WAITING_FOR_ARRIVAL, Status.CASHIER, Status.CASHIER_COMPLETED, Status.PK243, Status.FREE, Status.VALIDATION, Status.LABORATORY_COMPLETED, Status.NURSE_COMPLETED));
//        } else if (user.getRoles().contains(roleRepository.findByName("lis_nurse"))) {
//            statuses.addAll(List.of(Status.NURSE, Status.NURSE_COMPLETED));
//        } else if (user.getRoles().contains(roleRepository.findByName("lis_labrant"))) {
//            statuses.addAll(List.of(Status.LABORATORY, Status.VALIDATION, Status.LABORATORY_COMPLETED));
//        } else if (user.getRoles().contains(roleRepository.findByName("lis_director"))) {
//            statuses.addAll(List.of(Status.ACTIVE, Status.REGISTERED, Status.NURSE, Status.LABORATORY, Status.WAITING_FOR_ARRIVAL, Status.CASHIER, Status.CASHIER_COMPLETED, Status.PK243, Status.FREE, Status.VALIDATION, Status.LABORATORY_COMPLETED, Status.NURSE_COMPLETED));
//        }
        // TODO utachnit qilish kere rollani
        return statuses;
    }


    public ResponseEntity<?> attachToOrganization(Long id, List<Long> organizationIds) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new RecordNotFoundException("User with id: " + id + " not found")
        );
        Set<Organization> organizations = new HashSet<>();
        for (Long organizationId : organizationIds) {
            organizationRepository.findById(organizationId)
                    .ifPresent(organizations::add);
        }
        user.setParentOrganizations(organizations);
        return ResponseEntity.ok(
                new UserResponseDTO(
                        userRepository.save(user)
                )
        );
    }

    public ResponseEntity<ResponseDto<PageWrapper>> search(Long organizationId ,Pagination<Search> pagination) {
        Page<UserResponseDTO> userPage;
        if (pagination.getSearch().getValue() != null && !pagination.getSearch().getValue().isEmpty()) {
            userPage = search(organizationId, pagination.getSearch().getValue().toLowerCase(), pagination);
        } else {
            userPage = search(organizationId, "", pagination);
        }
        return ResponseEntity.ok(new ResponseDto<>(ResponseDto.States.SUCCESS, HttpStatus.OK, 200, null, PageWrapper.builder().list(userPage.getContent()).total(userPage.getTotalElements()).build()));
    }

    public Page<UserResponseDTO> search(Long organizationId, String value, Pagination<Search> pagination) {
        Pageable paging = Utils.getPageableFromPagination(pagination);
        Page<UserResponseDTO> pagedResult;
        Organization organization = organizationRepository.findById(organizationId).orElseThrow(() -> new RecordNotFoundException("Organization not found with id " + organizationId));
        if (!value.isEmpty()) {
            pagedResult = userRepository.findAllBySearch(value, organization, paging);
        } else {
            pagedResult = userRepository.findAllUsers(organization, paging);
        }
        return pagedResult;
    }

    public ResponseEntity<?> get(Long id) {
        User user = userRepository.findActiveById(id).orElseThrow(
                () -> new RecordNotFoundException("User with id: " + id + " not found")
        );
        return ResponseEntity.ok(
                new UserResponseDTO(user)
        );
    }

    public ResponseEntity<?> generateUniqueUsername() {
        while (true) {
            String username = com.abdulatif.emis.utility.Utils.generateUnique();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.ok(username);
            }
        }
    }
}

