package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.annotations.CurrentUser;
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
@Tag(name = "User", description = "API для работы с пользователями")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Получить всех пользователей",
            description = "Получает список всех пользователей с возможностью пагинации.",
            parameters = {
                    @Parameter(name = "page", description = "Номер страницы для пагинации"),
                    @Parameter(name = "size", description = "Количество пользователей на странице для пагинации"),
                    @Parameter(name = "Accept-Language", description = "Код языка для локализации (например, 'ru', 'uz', 'uzCyril')", required = true)
            })
    @GetMapping("/all")
    public ApiResponsePage getAllUsers(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            HttpServletRequest request
    ) {
        return userService.getAllUsers(page, size, request);
    }

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Получает пользователя по заданному идентификатору (ID).",
            parameters = {
                    @Parameter(name = "id", description = "ID пользователя", required = true)})
    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(@PathVariable long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @GetMapping("/me")
    public ApiResponse<User> getCurrentUser(@CurrentUser User user) {
        return userService.getByUuid(user.getUuid());
    }

    @Operation(
            summary = "Обновить пользователя из MIS",
            description = "Обновляет данные пользователя, используя информацию из MIS по UUID пользователя.",
            parameters = {
                    @Parameter(name = "uuid", description = "UUID пользователя для обновления", required = true),
                    @Parameter(name = "Authorization", description = "JWT токен для авторизации", required = true)
            })
    @PutMapping("/{uuid}/update-from-mis")
    public ApiResponse<User> updateUserFromMis(
            @PathVariable UUID uuid,
            @RequestBody User user,
            @Parameter(hidden = true) @RequestHeader(AUTHORIZATION) String token
    ) {
        return userService.updateUserFromMis(uuid, user, token);
    }
}
