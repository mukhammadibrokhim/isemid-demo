package uz.uzinfocom.app.modules.form129.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.form129.application.command.accept.AcceptForm129Service;
import uz.uzinfocom.app.modules.form129.application.command.create.CreateForm129Service;
import uz.uzinfocom.app.modules.form129.application.command.reject.RejectForm129Service;
import uz.uzinfocom.app.modules.form129.web.dto.request.AcceptForm129Request;
import uz.uzinfocom.app.modules.form129.web.dto.request.CreateForm129Request;
import uz.uzinfocom.app.modules.form129.web.dto.request.RejectForm129Request;
import uz.uzinfocom.app.modules.form129.web.dto.response.CreateForm129Response;
import uz.uzinfocom.app.modules.form129.web.dto.response.Form129StatusResponse;
import uz.uzinfocom.app.modules.form129.web.mapper.Form129WebMapper;
import uz.uzinfocom.app.modules.form129.web.resolvers.Form129SourceResolver;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.platform.security.context.SourceHeader;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(
        name = "Form 129",
        description = "Управление формой №129 — извещением лаборатории в комитет санитарно-эпидемиологического "
                + "благополучия и общественного здоровья о результатах исследований (сифилис, гепатит B, "
                + "бруцеллёз): создание, приём и отклонение получателем."
)
@RequestMapping(ApiPaths.Form129.ROOT)
public class Form129CommandController {

    private final CreateForm129Service createForm129Service;
    private final AcceptForm129Service acceptForm129Service;
    private final RejectForm129Service rejectForm129Service;
    private final Form129WebMapper form129WebMapper;
    private final Form129SourceResolver sourceResolver;
    private final MessageResolver messageResolver;

    @Operation(
            summary = "Создать форму №129",
            description = "Регистрирует новую форму №129 и связанного пациента (если он ещё не зарегистрирован "
                    + "в системе). Начальный статус формы — SENT. Документ не поддерживает вложения — только "
                    + "структурированные сведения."
    )
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CreateForm129Response> create(
            @Parameter(description = "Источник поступления формы (заполняется автоматически по заголовку запроса).")
            @RequestHeader(value = SourceHeader.X_SOURCE, required = false) String sourceHeader,
            @Valid @RequestBody CreateForm129Request request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.created"),
                form129WebMapper.toResponse(createForm129Service.create(
                        form129WebMapper.toCommand(request, sourceResolver.resolve(sourceHeader))
                ))
        );
    }

    @Operation(
            summary = "Принять форму №129 (получатель)",
            description = "Получатель подтверждает приём формы: переводит её из статуса SENT в ACCEPTED. "
                    + "Доступно только организации-получателю."
    )
    @PatchMapping(ApiPaths.Form129.ACCEPT)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Form129StatusResponse> accept(
            @Parameter(description = "Идентификатор формы №129.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody(required = false) AcceptForm129Request request
    ) {
        AcceptForm129Request effectiveRequest = request != null ? request : new AcceptForm129Request(null);
        return ApiResponse.success(
                messageResolver.resolve("common.accepted"),
                form129WebMapper.toResponse(acceptForm129Service.accept(
                        form129WebMapper.toCommand(id, effectiveRequest)
                ))
        );
    }

    @Operation(
            summary = "Отклонить форму №129 (получатель)",
            description = "Переводит форму в статус CANCELED с обязательным указанием причины. Доступно только "
                    + "организации-получателю, и только пока форма ещё не принята (статус SENT)."
    )
    @PatchMapping(ApiPaths.Form129.REJECT)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Form129StatusResponse> reject(
            @Parameter(description = "Идентификатор формы №129.", required = true)
            @PathVariable @Positive Long id,
            @Valid @RequestBody RejectForm129Request request
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.canceled"),
                form129WebMapper.toResponse(rejectForm129Service.reject(
                        form129WebMapper.toCommand(id, request)
                ))
        );
    }
}
