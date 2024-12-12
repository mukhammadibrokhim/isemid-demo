package com.abdulatif.emis.controller;

import com.abdulatif.emis.dto.organization.OrganizationDTO;
import com.abdulatif.emis.model.util.Pagination;
import com.abdulatif.emis.model.util.Search;
import com.abdulatif.emis.service.OrganizationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Transactional
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/organization")
@SecurityRequirement(name = "Bearer Authentication")
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping("/list-search")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> listSearch(
            @RequestBody Pagination<Search> pagination
    ) {
        return organizationService.search(pagination);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> create(
            @RequestBody OrganizationDTO organizationDTO
    ) {
        return organizationService.create(organizationDTO);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> update(
            @RequestBody OrganizationDTO organizationDTO,
            @PathVariable Long id
    ) {
        return organizationService.update(id, organizationDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> delete(
            @PathVariable Long id
    ) {
        return organizationService.delete(id);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> get(
            @PathVariable Long id
    ) {
        return organizationService.get(id);
    }

    @GetMapping("/departments/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getDepartments(
            @PathVariable String uuid
    ) {
        return organizationService.getDepartmentsByOrganizationUuid(uuid);
    }

}
