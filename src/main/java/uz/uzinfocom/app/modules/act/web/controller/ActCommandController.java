package uz.uzinfocom.app.modules.act.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.modules.act.application.command.ActCommandService;
import uz.uzinfocom.app.modules.act.application.command.ActLisSendService;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.ActDetailResponse;
import uz.uzinfocom.app.modules.act.web.dto.request.ActRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.DeleteActRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.SendActToLisRequest;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

import java.util.Map;
import java.util.Optional;

/**
 * Exclusively {@code /v1/acts/*} — attaching an act to a card lives in
 * {@link CardActCommandController} instead, so this stays a pure "Act" tag
 * in Swagger, matching how {@code CardCommandController} is organized.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Act.ROOT)
@Tag(
        name = "Act",
        description = "Управление жизненным циклом акта: свободное заполнение прикреплённым сотрудником, "
                + "передача в LIS."
)
public class ActCommandController {

    private final ActCommandService actCommandService;
    private final ActLisSendService actLisSendService;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Сохранить акт",
            description = "Сохраняет данные акта по его типу (аналогично карте). Доступно прикреплённому "
                    + "сотруднику в любое время и сколько угодно раз, пока акт ещё не отправлен в LIS. "
                    + "Переводит акт в статус IN_PROGRESS. Возвращает полную детальную информацию по акту "
                    + "после сохранения."
    )
    @PutMapping(ApiPaths.Act.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ActDetailResponse> update(
            @Parameter(description = "Идентификатор акта.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody ActRequest request
    ) {
        ActDetailResponse updated = actCommandService.update(id, request);
        return ApiResponse.success(messageResolver.resolve("common.updated"), updated);
    }

    @Operation(
            summary = "Отметить акт готовым",
            description = "Переводит заполненный акт из IN_PROGRESS (или SEND_FAILED, если предыдущая отправка "
                    + "не удалась и ничего исправлять не требуется) в READY — подготовка к отправке в LIS."
    )
    @PatchMapping(ApiPaths.Act.READY)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> markReady(
            @Parameter(description = "Идентификатор акта.", required = true)
            @PathVariable @Positive Long id
    ) {
        actCommandService.markReady(id);
        return ApiResponse.success(messageResolver.resolve("common.updated"));
    }

    @Operation(
            summary = "Отправить акт в LIS",
            description = "Отправляет готовый (READY) акт в LIS указанной лаборатории. При успехе акт переходит "
                    + "в статус SENT и ожидает ответа через callback. При сбое (сеть, отказ LIS, некорректный "
                    + "ответ) акт переходит в SEND_FAILED с указанием причины — его можно поправить и "
                    + "отправить повторно."
    )
    @PostMapping(ApiPaths.Act.SEND_TO_LIS)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> sendToLis(
            @Parameter(description = "Идентификатор акта.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody SendActToLisRequest request
    ) {
        actLisSendService.send(id, request);
        return ApiResponse.success(messageResolver.resolve("common.updated"));
    }

    @Operation(
            summary = "Приём ответа от LIS",
            description = "Callback-эндпоинт, на который LIS отправляет результат обработки акта — тот же "
                    + "адрес, что был передан LIS в поле redirectUrl при отправке акта. Аутентифицируется так "
                    + "же, как остальной API (SSO), отдельного механизма для LIS не заводится. Переводит акт "
                    + "из SENT в COMPLETED и сохраняет ответ целиком в формате JSON."
    )
    @PostMapping(ApiPaths.Act.LIS_CALLBACK)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> receiveLisResponse(
            @Parameter(description = "Идентификатор акта.", required = true)
            @PathVariable @Positive Long id,
            @Schema(description = "Ответ LIS по акту, в исходном виде.")
            @RequestBody Map<String, Object> body
    ) {
        Long lisActId = extractLisActId(body);
        actCommandService.receiveLisResponse(id, lisActId, body);
        return ApiResponse.success(messageResolver.resolve("common.updated"));
    }

    /**
     * LIS's own act id, echoed in its callback body as a top-level
     * {@code "id"} field — mirrors the legacy callback handler's extraction
     * exactly, since this is LIS's contract, not ours to redesign.
     */
    private Long extractLisActId(Map<String, Object> body) {
        return Optional.ofNullable(body.get("id"))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::longValue)
                .orElse(null);
    }

    @Operation(
            summary = "Удалить акт",
            description = "Удаление возможно только пока акт ещё не отправлен в LIS (статусы NEW, IN_PROGRESS, "
                    + "READY, SEND_FAILED). Это мягкое удаление — запись остаётся в базе с отметкой об удалении."
    )
    @DeleteMapping(ApiPaths.Act.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> delete(
            @Parameter(description = "Идентификатор акта.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody DeleteActRequest request
    ) {
        actCommandService.delete(id, request.reason());
        return ApiResponse.success(messageResolver.resolve("common.deleted"));
    }
}
