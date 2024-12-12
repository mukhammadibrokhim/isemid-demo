package com.abdulatif.emis.service;

import com.abdulatif.emis.dto.organization.OrganizationDTO;
import com.abdulatif.emis.exception.RecordBadRequestException;
import com.abdulatif.emis.exception.RecordNotFoundException;
import com.abdulatif.emis.model.enums.Status;
import com.abdulatif.emis.model.organization.Organization;
import com.abdulatif.emis.model.util.*;
import com.abdulatif.emis.repository.organization.OrganizationRepository;
import com.abdulatif.emis.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserService userService;

    public ResponseEntity<ResponseDto<PageWrapper>> search(Pagination<Search> pagination) {
        Page<Organization> organizationPage;
        if (pagination.getSearch().getValue() != null && !pagination.getSearch().getValue().isEmpty()) {
            organizationPage = search(pagination.getSearch().getValue().toLowerCase(), pagination);
        } else {
            organizationPage = search("", pagination);
        }
        List<Organization> organizations = organizationPage.getContent();
        List<OrganizationDTO> organizationDTOS = new ArrayList<>(organizations.size());
        for (Organization organization : organizations) {
            organizationDTOS.add(getWithChildren(organization));
        }
        return ResponseEntity.ok(new ResponseDto<>(ResponseDto.States.SUCCESS, HttpStatus.OK, 200, null, PageWrapper.builder().list(organizationDTOS).total(organizationPage.getTotalElements()).build()));
    }

    public Page<Organization> search(String value, Pagination<Search> pagination) {
        Pageable paging = Utils.getPageableFromPagination(pagination);
        Page<Organization> pagedResult;
        if (!value.isEmpty()) {
            pagedResult = organizationRepository.findAllBySearch(value, paging);
        } else {
            pagedResult = organizationRepository.findAllOrganizations(paging);
        }
        return pagedResult;
    }


    public ResponseEntity<?> create(OrganizationDTO organizationDTO) {
        organizationRepository.findByName(organizationDTO.getName()).ifPresent(organization -> {
            throw new RecordBadRequestException("Organization with name " + organizationDTO.getName() + " already exists");
        });
        Organization parent = organizationRepository.findById(organizationDTO.getParentId()).orElse(null);
        Organization organization = organizationRepository.save(
                Organization.builder()
                        .name(organizationDTO.getName())
                        .shortName(organizationDTO.getShortName())
                        .status(Status.ACTIVE)
                        .parent(parent)
                        .address(organizationDTO.getAddress())
                        .build()
        );
        organization.setHierarchy(parent != null ? parent.getHierarchy() + "," + organization.getId() : String.valueOf(organization.getId()));
        return ResponseEntity.ok(
                organizationRepository.save(organization)
        );
    }

    public ResponseEntity<?> update(Long id, OrganizationDTO organizationDTO) {
        Organization organization = organizationRepository.findById(id).orElseThrow(
                () -> new RecordNotFoundException("Organization with id " + id + " not found")
        );
        if (!organization.getName().equals(organizationDTO.getName())) {
            organizationRepository.findByName(organizationDTO.getName()).ifPresent((organization1 -> {
                throw new RecordBadRequestException("Organization with name " + organizationDTO.getName() + " already exists");
            }));
        }
        Organization parent = organizationRepository.findById(organizationDTO.getParentId()).orElse(null);
        organization.setName(organizationDTO.getName());
        organization.setShortName(organizationDTO.getShortName());
        organization.setAddress(organizationDTO.getAddress());
        if (parent != null) {
            organization.setParent(parent);
            organization.setHierarchy(parent.getHierarchy() + "," + organization.getId());
        }
        return ResponseEntity.ok(
                organizationRepository.save(organization)
        );
    }

    public ResponseEntity<?> delete(Long id) {
        Organization organization = organizationRepository.findById(id).orElseThrow(
                () -> new RecordNotFoundException("Organization with id " + id + " not found")
        );
        organization.setStatus(Status.DELETED);
        organizationRepository.save(organization);
        return ResponseEntity.ok("SUCCESS");
    }


    public ResponseEntity<?> get(Long id) {
        Organization organization = organizationRepository.findActiveById(id).orElseThrow(
                () -> new RecordNotFoundException("Organization with id " + id + " not found")
        );
        return ResponseEntity.ok(organization);
    }

    public OrganizationDTO getWithChildren(Organization organization) {
        List<Organization> children = organizationRepository.findOrganizationChildrenById(organization.getId());
        List<OrganizationDTO> organizationDtos = new ArrayList<>();
        if (children != null && !children.isEmpty()) {
            for (Organization organizationChild : children) {
                OrganizationDTO withChildren = getWithChildren(organizationChild);
                organizationDtos.add(withChildren);
            }
        }
        OrganizationDTO organizationDto = new OrganizationDTO(organization);
        organizationDto.setChildren(organizationDtos);
        return organizationDto;
    }

    public ResponseEntity<?> getDepartmentsByOrganizationUuid(String uuid) {
        return ResponseEntity.ok(organizationRepository.findAllChildByUuid(uuid));
    }
}
