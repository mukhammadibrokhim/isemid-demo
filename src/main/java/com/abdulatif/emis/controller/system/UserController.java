package com.abdulatif.emis.controller.system;

import com.abdulatif.emis.model.util.Pagination;
import com.abdulatif.emis.model.util.Search;
import com.abdulatif.emis.service.user.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/user")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/list-search/{organizationId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> listSearch(
            @PathVariable Long organizationId,
            @RequestBody Pagination<Search> pagination
    ) {
        return userService.search(organizationId, pagination);
    }

    @PostMapping("/attach-to-organization/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> attachToOrganization(
            @PathVariable Long id,
            @RequestBody List<Long> organizationIds
    ) {
        return userService.attachToOrganization(id, organizationIds);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getById(
            @PathVariable Long id
    ) {
        return userService.get(id);
    }

    @GetMapping("/generate-unique-username")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> generateUniqueUsername() {
        return userService.generateUniqueUsername();
    }

}
