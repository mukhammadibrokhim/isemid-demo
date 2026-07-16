package uz.uzinfocom.app.modules.form0581.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import uz.uzinfocom.app.modules.form0581.application.query.Form0581Filter;
import uz.uzinfocom.app.modules.form0581.application.query.Form0581QueryService;
import uz.uzinfocom.app.modules.form0581.application.query.dto.Form0581TableResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.detail.Form0581DetailResponse;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;
import uz.uzinfocom.app.shared.dto.response.ApiResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponse;
import uz.uzinfocom.app.shared.dto.response.PagedResponseAssembler;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(
        name = "Form 058-1",
        description = "Управление формой №058-1 — экстренным извещением о случае, подозрительном на бешенство "
                + "(укус/царапина/ослюнение животным): создание, редактирование, удаление, утверждение и аннулирование."
)
@RequestMapping(ApiPaths.Form0581.ROOT)
public class Form0581QueryController {

    private final Form0581QueryService form0581QueryService;
    private final MessageResolver messageResolver;
    private final PagedResponseAssembler pagedResponseAssembler;

    @Operation(
            summary = "Список форм №058-1",
            description = "Возвращает постраничный список форм с возможностью фильтрации по статусу, "
                    + "организациям-отправителю/получателю и другим параметрам."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<Form0581TableResponse> findAll(
            @ParameterObject @Valid Form0581Filter filter,
            HttpServletRequest httpRequest
    ) {
        return pagedResponseAssembler
                .toResponse(form0581QueryService.findAll(filter), messageResolver.resolve("common.success"), httpRequest);
    }

    @Operation(
            summary = "Найти форму №058-1 по документу пациента",
            description = "Возвращает детальные сведения о форме по значению документа, удостоверяющего "
                    + "личность пациента (ПИНФЛ, паспорт и т. п.)."
    )
    @GetMapping(ApiPaths.Form0581.BY_DOCUMENT_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Form0581DetailResponse> byDocumentValue(
            @Parameter(description = "Значение документа, удостоверяющего личность пациента.", required = true)
            @RequestParam @NotBlank String documentValue
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form0581QueryService.getByDocumentValue(documentValue)
        );
    }

    @Operation(
            summary = "Получить форму №058-1 по идентификатору",
            description = "Возвращает полные детальные сведения по форме, включая сведения о происшествии, "
                    + "животном и данные пациента."
    )
    @GetMapping(ApiPaths.Form0581.BY_ID)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Form0581DetailResponse> byId(
            @Parameter(description = "Идентификатор формы №058-1.", required = true)
            @PathVariable @Positive Long id
    ) {
        return ApiResponse.success(
                messageResolver.resolve("common.success"),
                form0581QueryService.getById(id)
        );
    }
}
