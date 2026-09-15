package uz.uzinfocom.app.modules.form129.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.modules.form129.application.query.Form129Filter;
import uz.uzinfocom.app.modules.form129.application.query.Form129QueryService;
import uz.uzinfocom.app.modules.form129.application.query.dto.Form129TableResponse;
import uz.uzinfocom.app.modules.form129.application.query.dto.detail.Form129DetailResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(
        name = "Form 129",
        description = "Управление формой №129 — извещением лаборатории в комитет санитарно-эпидемиологического "
                + "благополучия и общественного здоровья о результатах исследований."
)
@RequestMapping(ApiPaths.Form129.ROOT)
public class Form129QueryController {

    private final Form129QueryService form129QueryService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Список форм №129",
            description = "Возвращает постраничный список форм с возможностью фильтрации по статусу, "
                    + "организациям-отправителю/получателю, направлению (chiquvchi/kiruvchi) и другим параметрам."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<Form129TableResponse> findAll(
            @ParameterObject @Valid Form129Filter filter,
            HttpServletRequest httpRequest
    ) {
        return pagedResponseAssembler
                .toResponse(form129QueryService.findAll(filter), messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Получить форму №129 по идентификатору",
            description = "Возвращает полные детальные сведения по форме, включая результаты лабораторных "
                    + "исследований и данные пациента."
    )
    @GetMapping(ApiPaths.Form129.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Form129DetailResponse> byId(
            @Parameter(description = "Идентификатор формы №129.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form129QueryService.getById(id)
        );
    }
}
