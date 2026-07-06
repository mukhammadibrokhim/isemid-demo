package uz.uzinfocom.isemid.features.user.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.user.dtos.requests.UserFilterRequest;
import uz.uzinfocom.isemid.features.user.dtos.requests.UserRequest;
import uz.uzinfocom.isemid.features.user.dtos.responses.UserMeResponse;
import uz.uzinfocom.isemid.features.user.dtos.responses.UserResponse;
import uz.uzinfocom.isemid.features.user.mappers.UserMapper;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;
import uz.uzinfocom.isemid.features.user.services.UserService;

import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uz.uzinfocom.isemid.commons.utils.RequestUrlUtils.getRequestUrl;

@RestController
@RequestMapping(ApiPaths.User.ROOT)
@RequiredArgsConstructor
@Tag(name = "User", description = "API для работы с пользователями")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final MessageUtil messageUtil;

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
            @Valid @ModelAttribute UserFilterRequest filter,
            @CurrentUser User currentUser,
            HttpServletRequest request
    ) {
        Page<User> userPage = userService.filter(filter, currentUser);

        return PagedResponse.build(
                filter.getPage(),
                userPage.map(userMapper::toResponse),
                getRequestUrl(request),
                messageUtil
        );
    }

    @Operation(summary = "Получить пользователя по ID", description = "Получает пользователя по заданному идентификатору (ID).")
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable long id) {
        return ApiResponse.success(messageUtil, userMapper.toResponse(userService.getById(id)));
    }

    @Operation(summary = "Обновить данные пользователя", description = "Обновляет данные пользователя по заданному ID.")
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(@PathVariable long id, @Valid @RequestBody UserRequest request) {
        return ApiResponse.success(messageUtil, userMapper.toResponse(userService.update(id, request)));
    }

    @Operation(summary = "Получить данные текущего пользователя", description = "Возвращает информацию о текущем аутентифицированном пользователе.")
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<UserMeResponse> getCurrentUser(@CurrentUser User user) {
        return ApiResponse.success(messageUtil, userMapper.toUserMeResponse(user));
    }

    @Operation(summary = "Обновить пользователя из MIS", description = "Обновляет данные пользователя, используя информацию из MIS по UUID пользователя.")
    @PutMapping("/{uuid}/update-from-mis")
    public ApiResponse<UserResponse> updateUserFromMis(
            @PathVariable UUID uuid,
            @Parameter(hidden = true) @RequestHeader(AUTHORIZATION) String token
    ) {
        return ApiResponse.success(messageUtil, userMapper.toResponse(userService.updateUserFromMis(uuid, token)));
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ApiResponse<?> deleteUserRole(@PathVariable long userId, @PathVariable long roleId) {
        userService.removeRoleFromUser(userId, roleId);
        return ApiResponse.deleteSuccess(messageUtil);
    }
}
