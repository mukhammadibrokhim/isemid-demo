package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.annotations.CurrentUser;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.services.RoleService;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.ROLE;
import static uz.uzinfocom.isemid.utils.AppConstant.PAGE;
import static uz.uzinfocom.isemid.utils.AppConstant.SIZE;
import static uz.uzinfocom.isemid.utils.CommonUtils.getFullURL;


@RestController
@RequestMapping(ROLE)
@RequiredArgsConstructor
@Tag(name = "Role", description = "API for Roles")
public class RoleController {

    final RoleService roleService;

    @GetMapping("/all")
    public ApiResponsePage getAllRoles(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            @CurrentUser User currentUser,
            HttpServletRequest request
    ) {
        return roleService.getAllRolesPage(page, size, getFullURL(request),currentUser);
    }
}
