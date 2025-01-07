package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.services.UserService;

import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uz.uzinfocom.isemid.utils.ApiUrlConstant.USER;
import static uz.uzinfocom.isemid.utils.AppConstant.PAGE;
import static uz.uzinfocom.isemid.utils.AppConstant.SIZE;

@RestController
@RequestMapping(USER)
@RequiredArgsConstructor
@Tag(name = "User")
public class UserController {

    private final UserService userService;

    @GetMapping("/all")
    public ApiResponsePage getAllUsers(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            HttpServletRequest request
    ) {
        return userService.getAllUsers(page,size,request);
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable long id) {
        return userService.getUserById(id);
    }

    @Operation
    @PutMapping("/{uuid}/update-from-mis")
    public ApiResponse<User> updateUserFromMis(
            @PathVariable UUID uuid,
            @RequestBody User user,
            @Parameter(hidden = true) @RequestHeader(AUTHORIZATION) String token

    ) {
        return userService.updateUserFromMis(uuid, user,token);
    }
}
