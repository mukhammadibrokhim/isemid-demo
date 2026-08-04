package uz.uzinfocom.app.platform.notification.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.notification.application.NotificationCommandService;
import uz.uzinfocom.app.platform.notification.application.NotificationQueryService;
import uz.uzinfocom.app.platform.notification.application.NotificationStreamService;
import uz.uzinfocom.app.platform.notification.application.dto.NotificationFilterRequest;
import uz.uzinfocom.app.platform.notification.application.dto.NotificationResponse;
import uz.uzinfocom.app.platform.notification.domain.NotificationType;
import uz.uzinfocom.app.platform.security.annotation.CurrentUser;
import uz.uzinfocom.app.platform.security.principal.PrincipalUser;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

import java.util.Map;

/**
 * Personal, server-scoped notification surface — never trust a client-supplied user id,
 * always resolve from the authenticated principal (same rule as {@code Card.MINE}/{@code Act.MINE}).
 */
@Tag(
        name = "Notifications",
        description = "Уведомления текущего пользователя: список, счетчик непрочитанных, "
                + "отметка прочтения и поток обновлений через Server-Sent Events."
)
@Validated
@RestController
@RequestMapping(ApiPaths.Notification.ROOT)
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;
    private final NotificationStreamService notificationStreamService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Мои уведомления",
            description = "Постраничный список уведомлений текущего пользователя, новые сверху."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<NotificationResponse> findMine(
            @ParameterObject @Valid NotificationFilterRequest filter,
            @CurrentUser PrincipalUser principal,
            HttpServletRequest httpRequest
    ) {
        Page<NotificationResponse> page = notificationQueryService.findMine(principal.id(), filter);
        return pagedResponseAssembler.toResponse(page, messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Счетчик непрочитанных уведомлений",
            description = "Значение для бейджа — количество непрочитанных уведомлений текущего пользователя."
    )
    @GetMapping(ApiPaths.Notification.UNREAD_COUNT)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> unreadCount(@CurrentUser PrincipalUser principal) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                notificationQueryService.countUnread(principal.id())
        );
    }

    @Operation(
            summary = "Счетчик непрочитанных уведомлений по типам",
            description = "Количество непрочитанных уведомлений текущего пользователя, "
                    + "сгруппированное по каждому типу — все типы присутствуют в ответе, "
                    + "отсутствующие считаются нулём."
    )
    @GetMapping(ApiPaths.Notification.UNREAD_COUNT_BY_TYPE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<NotificationType, Long>> unreadCountByType(@CurrentUser PrincipalUser principal) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                notificationQueryService.countUnreadByType(principal.id())
        );
    }

    @Operation(
            summary = "Отметить уведомление как прочитанное"
    )
    @PostMapping(ApiPaths.Notification.BY_ID_READ)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> markRead(
            @Parameter(description = "Идентификатор уведомления.", required = true)
            @PathVariable @Positive Long id,
            @CurrentUser PrincipalUser principal
    ) {
        notificationCommandService.markRead(id, principal.id());
        return ApiResponse.success(messageResolver.resolve("common.updated"));
    }

    @Operation(
            summary = "Отметить все уведомления как прочитанные"
    )
    @PostMapping(ApiPaths.Notification.READ_ALL)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> markAllRead(@CurrentUser PrincipalUser principal) {
        notificationCommandService.markAllRead(principal.id());
        return ApiResponse.success(messageResolver.resolve("common.updated"));
    }

    @Operation(
            summary = "Поток уведомлений (SSE)",
            description = "Server-Sent Events поток с количеством непрочитанных уведомлений текущего пользователя."
    )
    @GetMapping(ApiPaths.Notification.STREAM)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter stream(@CurrentUser PrincipalUser principal) {
        return notificationStreamService.stream(principal.id());
    }
}
