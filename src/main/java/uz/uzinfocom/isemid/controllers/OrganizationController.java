package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.Organization;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.annotations.CurrentUser;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.services.OrganizationService;

import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uz.uzinfocom.isemid.utils.ApiUrlConstant.ORGANIZATION;
import static uz.uzinfocom.isemid.utils.AppConstant.PAGE;
import static uz.uzinfocom.isemid.utils.AppConstant.SIZE;

@RestController
@RequestMapping(ORGANIZATION)
@Tag(name = "Organization")
@RequiredArgsConstructor
public class OrganizationController {
    final OrganizationService organizationService;

    @GetMapping("/all")
    public ApiResponsePage getAllOrganizations(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            HttpServletRequest request
    ) {
        return organizationService.getAllOrganizationsByFilter(page,size,request);
    }

    @GetMapping("/{id}")
    public ApiResponse<Organization> getOrganizationById(@PathVariable long id) {
        return organizationService.getById(id);
    }

    @PutMapping("/{uuid}/update-from-mis")
    public ApiResponse<Organization> updateOrganization(
            @PathVariable UUID uuid,
            @CurrentUser User currentUser,
            @Parameter(hidden = true) @RequestHeader(AUTHORIZATION) String token
    ) {
        return organizationService.updateUserFromMis(uuid, currentUser, token);
    }
}
