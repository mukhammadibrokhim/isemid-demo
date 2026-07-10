package uz.uzinfocom.app.modules.form058.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.app.modules.form058.application.query.Form058Filter;
import uz.uzinfocom.app.modules.form058.application.query.Form058QueryService;
import uz.uzinfocom.app.modules.form058.application.query.dto.Form058TableResponse;
import uz.uzinfocom.app.modules.form058.application.query.dto.detail.Form058DetailResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.response.ApiResponse;
import uz.uzinfocom.app.shared.response.PagedResponse;
import uz.uzinfocom.app.shared.response.PagedResponseAssembler;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(
        name = "Form 058",
        description = "Управление формой №058 — экстренным извещением об инфекционном заболевании: "
                + "создание, редактирование, удаление, утверждение и аннулирование."
)
@RequestMapping(ApiPaths.Form058.ROOT)
public class Form058QueryController {

    private final Form058QueryService form058QueryService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Список форм №058",
            description = "Возвращает постраничный список форм с возможностью фильтрации по статусу, "
                    + "организациям-отправителю/получателю и другим параметрам."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<Form058TableResponse> findAll(
            @ParameterObject @Valid Form058Filter filter,
            HttpServletRequest httpRequest
    ) {
        return pagedResponseAssembler
                .toResponse(form058QueryService.findAll(filter), messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Найти форму №058 по ПИНФЛ пациента",
            description = "Возвращает детальные сведения о форме по идентификационному номеру физического "
                    + "лица (ПИНФЛ/nnuzb) пациента."
    )
    @GetMapping("/by-nnuzb")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Form058DetailResponse> byNnuzb(
            @Parameter(description = "Идентификационный номер физического лица (ПИНФЛ) пациента.", required = true)
            @RequestParam @NotBlank String nnuzb
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form058QueryService.getByNnuzb(nnuzb)
        );
    }

    @Operation(
            summary = "Получить форму №058 по идентификатору",
            description = "Возвращает полные детальные сведения по форме, включая клиническую, "
                    + "эпидемиологическую информацию и данные пациента."
    )
    @GetMapping(ApiPaths.Form058.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Form058DetailResponse> byId(
            @Parameter(description = "Идентификатор формы №058.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form058QueryService.getById(id)
        );
    }
}