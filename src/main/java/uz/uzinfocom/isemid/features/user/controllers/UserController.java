package uz.uzinfocom.isemid.features.user.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.features.user.annotation.CurrentUser;
import uz.uzinfocom.isemid.features.user.dtos.requests.UserRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.UserMeResponse;
import uz.uzinfocom.isemid.features.user.dtos.responses.UserResponse;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.services.UserService;

import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;
import static uz.uzinfocom.isemid.commons.utils.HttpUtil.getRequestUrl;

@RestController
@RequestMapping(ApiPaths.User.ROOT)
@RequiredArgsConstructor
@Tag(name = "User", description = "API для работы с пользователями")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Получить всех пользователей",
            description = "Получает список всех пользователей с возможностью пагинации и фильтрации по различным параметрам.",
            parameters = {
                    @Parameter(name = "page", description = "Номер страницы для пагинации", example = "0"),
                    @Parameter(name = "size", description = "Количество пользователей на странице", example = "10"),
                    @Parameter(name = "nnuzb", description = "Фильтр по nnuzb"),
                    @Parameter(name = "firstName", description = "Фильтр по имени"),
                    @Parameter(name = "lastName", description = "Фильтр по фамилии"),
                    @Parameter(name = "middleName", description = "Фильтр по отчеству"),
                    @Parameter(name = "phoneNumber", description = "Фильтр по номеру телефона"),
                    @Parameter(name = "accessLevelCode", description = "Фильтр по коду уровня доступа"),
                    @Parameter(name = "active", description = "Фильтр по статусу активности")
            }
    )
    @GetMapping("/all")
    public PagedResponse<?> getAllUsers(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false) String nnuzb,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String middleName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String accessLevelCode,
            @RequestParam(required = false) Boolean active,
            HttpServletRequest request
    ) {
        return userService.getFilteredUsers(page, size, nnuzb, firstName, lastName, middleName, phoneNumber, accessLevelCode, active, getRequestUrl(request));
    }

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Получает пользователя по заданному идентификатору (ID).",
            parameters = {
                    @Parameter(name = "id", description = "ID пользователя", required = true)
            }
    )
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable long id) {
        return userService.getById(id);
    }

    @Operation(
            summary = "Обновить данные пользователя",
            description = "Обновляет данные пользователя по заданному ID."
    )
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(@PathVariable long id, @Valid @RequestBody UserRequest reqUser) {
        return userService.update(id, reqUser);
    }

    @Operation(
            summary = "Получить данные текущего пользователя",
            description = "Возвращает информацию о текущем аутентифицированном пользователе."
    )
    @GetMapping("/me")
    public ApiResponse<UserMeResponse> getCurrentUser(@CurrentUser User user) {
        return userService.getByUuid(user);
    }

    @Operation(
            summary = "Обновить пользователя из MIS",
            description = "Обновляет данные пользователя, используя информацию из MIS по UUID пользователя.",
            parameters = {
                    @Parameter(name = "uuid", description = "UUID пользователя для обновления", required = true),
                    @Parameter(name = "Authorization", description = "JWT токен для авторизации", required = true)
            }
    )
    @PutMapping("/{uuid}/update-from-mis")
    public ApiResponse<UserResponse> updateUserFromMis(
            @PathVariable UUID uuid,
            @RequestBody UserRequest user,
            @Parameter(hidden = true) @RequestHeader(AUTHORIZATION) String token
    ) {
        return userService.updateUserFromMis(uuid, user, token);
    }
}
