package com.abdulatif.emis.controller.system;


import com.abdulatif.emis.exception.RecordNotFoundException;
import com.abdulatif.emis.model.user.Role;
import com.abdulatif.emis.model.user.User;
import com.abdulatif.emis.model.util.ResponseDto;
import com.abdulatif.emis.model.util.Utils;
import com.abdulatif.emis.service.user.RoleService;
import com.abdulatif.emis.service.user.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1")
public class AuthenticationController {

    private final RoleService roleService;
    private final UserService userService;

    @GetMapping("/auth-payload")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ResponseDto<Map<String, Object>>> user() {
        User user = Utils.currentUser(userService);
        if (user == null)
            throw new RecordNotFoundException("User not found");
        if (user.getRoles() == null || user.getRoles().isEmpty())
            throw new RecordNotFoundException("Access is denied");
        List<Role> roles = user.getRoles().stream().toList();
        List<String> roleNames = roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        List<Long> roleIds = roles.stream()
                .map(Role::getId)
                .toList();
        List<String> permissionsDto = roleService.findPermissionNames(roleIds);
        Map<String, Object> response = new HashMap<>();
        StringBuilder fullName = new StringBuilder();
        if (user.getFirstName() != null)
            fullName.append(user.getFirstName()).append(" ");

        if (user.getMiddleName() != null)
            fullName.append(user.getMiddleName()).append(" ");

        if (user.getLastName() != null)
            fullName.append(user.getLastName());
        response.put("fullName", fullName.toString());
        response.put("email", user.getEmail());
        response.put("inn", user.getInn());
        response.put("pinfl", user.getPnfl());
        response.put("phone", user.getPhone());
        response.put("address", user.getAddress());
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("role", roleNames);
        response.put("permissions", permissionsDto);
        response.put("organizations", user.getParentOrganizations());

        return ResponseEntity.ok(new ResponseDto<>(ResponseDto.States.SUCCESS, HttpStatus.OK, 200, null, response));
    }
}
