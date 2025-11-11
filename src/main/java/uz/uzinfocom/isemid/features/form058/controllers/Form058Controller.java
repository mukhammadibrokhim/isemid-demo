package uz.uzinfocom.isemid.features.form058.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.form058.dtos.requests.*;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058DetailsResponse;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058Response;
import uz.uzinfocom.isemid.features.form058.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.mappers.Form058Mapper;
import uz.uzinfocom.isemid.features.form058.mappers.Form058ResponseAdapter;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.form058.services.Form058Service;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.HttpStatus.CREATED;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;
import static uz.uzinfocom.isemid.commons.utils.HttpUtil.getRequestUrl;

@RestController
@RequestMapping(ApiPaths.Form058.ROOT)
@Tag(name = "Form 058", description = "API для работы с формой 058")
@RequiredArgsConstructor
public class Form058Controller {
    private final Form058Service form058Service;
    private final MessageUtil messageUtil;
    private final Form058Mapper form058Mapper;
    private final Form058ResponseAdapter formAdapter;

    @Operation(summary = "Создание формы 058", description = "Создаёт новую форму 058 с данными пациента и другой необходимой информацией.")
    @PostMapping
    @ResponseStatus(CREATED)
    public ApiResponse<Form058Response> create(@Valid @RequestBody Form058Request form058Request) {
        return ApiResponse.success(messageUtil, form058Mapper.toResponse(form058Service.create(form058Request)));
    }

    @Operation(summary = "Получение всех форм 058", description = "Возвращает постраничный список всех форм 058 с возможностью фильтрации по дате.")
    @GetMapping("/all")
    public PagedResponse<?> getAll(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String stateCode,
            @RequestParam(required = false) String cityCode,
            @RequestParam(required = false) String mkb10Code,
            @RequestParam(required = false) Long organizationId,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        Page<Form058> form058Page = form058Service.filter(page, size, fromDate, toDate, stateCode, cityCode, null, mkb10Code, organizationId, user, FilterType.ALL);
        Page<Form058DetailsResponse> adapted = form058Page.map(form -> {
            Form058DetailsResponse base = form058Mapper.toDetailsResponse(form);
            return formAdapter.adapt(base, form, FilterType.ALL);
        });
        return PagedResponse.build(page, adapted, getRequestUrl(request), messageUtil);
    }

    @Operation(summary = "Получение всех полученных форм 058", description = "Возвращает постраничный список всех форм 058, которые были получены, с возможностью фильтрации по дате.")
    @GetMapping("/received/all")
    public PagedResponse<?> getReceivedAll(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String stateCode,
            @RequestParam(required = false) String cityCode,
            @RequestParam(required = false) Boolean affiliation,
            @RequestParam(required = false) String mkb10Code,
            @RequestParam(required = false) Long organizationId,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        Page<Form058> form058Page = form058Service.filter(page, size, fromDate, toDate, stateCode, cityCode, affiliation, mkb10Code, organizationId, user, FilterType.RECEIVED);
        Page<Form058DetailsResponse> adapted = form058Page.map(form -> {
            Form058DetailsResponse base = form058Mapper.toDetailsResponse(form);
            return formAdapter.adapt(base, form, FilterType.RECEIVED);
        });
        return PagedResponse.build(page, adapted, getRequestUrl(request), messageUtil);
    }

    @Operation(summary = "Получение формы 058 по ID", description = "Возвращает данные конкретной формы 058 по заданному идентификатору.")
    @GetMapping("/{id}")
    public ApiResponse<Form058Response> get(@PathVariable long id, @CurrentUser User user) {
        return ApiResponse.success(messageUtil, form058Mapper.toResponse(form058Service.getById(id), user.getCurrentOrganization().getId()));
    }

    @Operation(summary = "Обновление формы 058", description = "Обновляет данные существующей формы 058 по заданному идентификатору.")
    @PutMapping("/{id}")
    public ApiResponse<Form058Response> update(
            @RequestBody Form058Request request,
            @PathVariable Long id,
            @CurrentUser User user
    ) {
        return ApiResponse.success(messageUtil, form058Mapper.toResponse(form058Service.update(request, id, user)));
    }

    @Operation(summary = "Назначение карты для формы 058", description = "Позволяет назначить карту (Card) для существующей формы 058.")
    @PostMapping("/assign-card")
    public ApiResponse<Form058Response> assignCard(@Valid @RequestBody Form058AssignCardRequest request, @CurrentUser User user) {
        return ApiResponse.success(messageUtil, form058Mapper.toResponse(form058Service.assignCard(request, user)));
    }

    @Operation(summary = "Изменение организации получателя формы 058", description = "Позволяет изменить только организацию, получившую форму 058, без изменения других данных формы.")
    @PatchMapping("/{formId}/change-received-org/{orgId}")
    public ApiResponse<?> changeReceivedOrg(
            @PathVariable Long formId, @PathVariable Long orgId
    ) {
        return ApiResponse.success(messageUtil, form058Mapper.toResponse(form058Service.changeReceivedOrd(formId, orgId)));
    }


    @Operation(summary = "Отмена формы 058", description = "Позволяет отменить (отклонить) форму 058 по её идентификатору с указанием причины.")
    @PatchMapping("/{formId}/cancel")
    public ApiResponse<?> cancel(
            @PathVariable Long formId,
            @RequestBody @Valid Form058CancelRequest request
    ) {
        return ApiResponse.success(
                messageUtil,
                form058Mapper.toResponse(form058Service.cancel(formId, request.getReason()))
        );
    }


    @Operation(summary = "Подтверждение формы 058", description = "Позволяет утвердить форму 058.")
    @PatchMapping("/{formId}/approve")
    public ApiResponse<?> approve(@PathVariable Long formId) {
        return ApiResponse.success(messageUtil, form058Mapper.toResponse(form058Service.approveForm(formId)));
    }

    @Operation(summary = "Отклонение формы 058", description = "Позволяет отклонить форму 058 с указанием причин.")
    @PutMapping("/{formId}/not-approve")
    public ApiResponse<?> notApprove(@PathVariable Long formId, @RequestBody Form058NotApproveRequest notApprove) {
        return ApiResponse.success(messageUtil, form058Mapper.toResponse(form058Service.notApprove(formId, notApprove)));
    }

    @Operation(
            summary = "Подтверждение диагноза в форме 058",
            description = """
                        Позволяет подтвердить диагноз, указанный в форме 058.
                        При необходимости можно изменить диагноз перед подтверждением.
                        Если новый диагноз не передан, будет подтверждён существующий.
                    """
    )
    @PutMapping("/{formId}/approve-diagnosis")
    public ApiResponse<Form058Response> approveDiagnosis(
            @PathVariable Long formId,
            @Valid @RequestBody Form058ChangeDiagnosisRequest request
    ) {
        Form058 form = form058Service.approveDiagnosis(formId, request);
        return ApiResponse.success(messageUtil, form058Mapper.toResponse(form));
    }

    @Operation(summary = "Удаление формы 058 по ID", description = "Удаляет форму 058 по заданному идентификатору.")
    @PreAuthorize("hasRole('ROLE_isemid_super_admin')")
    @DeleteMapping("/{formId}")
    public ApiResponse<?> deleteById(@PathVariable Long formId) {
        form058Service.deleteById(formId);
        return ApiResponse.deleteSuccess(messageUtil);
    }

    @GetMapping("/confirmation-stats")
    public ApiResponse<?> getConfirmationStats(
            @RequestParam(required = false) List<String> mkb10Codes,
            @RequestParam(required = false) List<String> regionCodes,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestHeader(value = ACCEPT_LANGUAGE, required = false) String locale,
            @CurrentUser User currentUser
    ) {
        return ApiResponse.success(messageUtil, form058Service.getConfirmationStats(mkb10Codes, regionCodes, fromDate, toDate, locale, currentUser));
    }
}

