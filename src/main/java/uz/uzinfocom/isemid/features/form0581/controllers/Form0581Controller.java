package uz.uzinfocom.isemid.features.form0581.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.card.dtos.responses.CardResponse;
import uz.uzinfocom.isemid.features.card.mappers.CardMapper;
import uz.uzinfocom.isemid.features.form058.constants.Form058Headers;
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581ApproveDiagnosisRequest;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581AssignCardRequest;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581CancelRequest;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581FilterRequest;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581NotApproveRequest;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581Request;
import uz.uzinfocom.isemid.features.form0581.dtos.responses.Form0581DetailsResponse;
import uz.uzinfocom.isemid.features.form0581.dtos.responses.Form0581Response;
import uz.uzinfocom.isemid.features.form0581.mappers.Form0581Mapper;
import uz.uzinfocom.isemid.features.form0581.mappers.Form0581ResponseAdapter;
import uz.uzinfocom.isemid.features.form0581.models.Form0581;
import uz.uzinfocom.isemid.features.form0581.services.Form0581Facade;
import uz.uzinfocom.isemid.features.form0581.services.Form0581Service;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.CREATED;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;
import static uz.uzinfocom.isemid.commons.utils.RequestUrlUtils.getRequestUrl;

@RestController
@RequestMapping(ApiPaths.Form0581.ROOT)
@Tag(name = "Form 058-1", description = "API для работы с формой 058-1 (укус животного/бешенство)")
@RequiredArgsConstructor
public class Form0581Controller {
    private final Form0581Service form0581Service;
    private final Form0581Facade form0581Facade;
    private final MessageUtil messageUtil;
    private final Form0581Mapper form0581Mapper;
    private final Form0581ResponseAdapter formAdapter;
    private final CardMapper cardMapper;

    @Operation(summary = "Создание формы 058-1", description = "Создаёт новую форму 058-1 с данными пациента и другой необходимой информацией.")
    @PostMapping
    @ResponseStatus(CREATED)
    public ApiResponse<Form0581Response> create(
            @Valid @RequestBody Form0581Request request,
            @RequestHeader(value = Form058Headers.X_SOURCE, required = false) String sourceHeader,
            @CurrentUser User currentUser
    ) {
        return ApiResponse.success(messageUtil, form0581Mapper.toResponse(form0581Service.create(request, sourceHeader, currentUser)));
    }

    @GetMapping("/all")
    public PagedResponse<?> getAll(
            @Valid @ModelAttribute @ParameterObject Form0581FilterRequest filterRequest,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        Page<Form0581> page = form0581Service.getAll(filterRequest, user, FilterType.SENDER);
        Page<Form0581DetailsResponse> adapted = page.map(form -> {
            Form0581DetailsResponse base = form0581Mapper.toDetailsResponse(form);
            return formAdapter.adapt(base, form, FilterType.SENDER);
        });
        return PagedResponse.build(filterRequest.page(), adapted, getRequestUrl(request), messageUtil);
    }

    @GetMapping("/received/all")
    public PagedResponse<?> getReceivedAll(
            @Valid @ModelAttribute @ParameterObject Form0581FilterRequest filterRequest,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        Page<Form0581> page = form0581Service.getAll(filterRequest, user, FilterType.RECEIVER);
        Page<Form0581DetailsResponse> adapted = page.map(form -> {
            Form0581DetailsResponse base = form0581Mapper.toDetailsResponse(form);
            return formAdapter.adapt(base, form, FilterType.RECEIVER);
        });
        return PagedResponse.build(filterRequest.page(), adapted, getRequestUrl(request), messageUtil);
    }

    @GetMapping("/by-nnuzb")
    public PagedResponse<?> getByNnuzb(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam String nnuzb,
            HttpServletRequest request
    ) {
        Page<Form0581> formPage = form0581Service.getByPatientNnuzb(page, size, nnuzb);

        Page<Form0581DetailsResponse> adapted = formPage.map(form -> {
            Form0581DetailsResponse base = form0581Mapper.toDetailsResponse(form);
            return formAdapter.adapt(base, form, FilterType.ALL);
        });

        return PagedResponse.build(page, adapted, getRequestUrl(request), messageUtil);
    }

    @Operation(summary = "Получение формы 058-1 по ID", description = "Возвращает данные конкретной формы 058-1 по заданному идентификатору.")
    @GetMapping("/{id}")
    public ApiResponse<Form0581Response> get(@PathVariable long id, @CurrentUser User user) {
        return ApiResponse.success(messageUtil, form0581Mapper.toResponse(form0581Service.getById(id), user.getCurrentOrganization().getId()));
    }

    @Operation(summary = "Обновление формы 058-1", description = "Обновляет данные существующей формы 058-1 по заданному идентификатору.")
    @PutMapping("/{id}")
    public ApiResponse<Form0581Response> update(
            @RequestBody Form0581Request request,
            @PathVariable Long id,
            @CurrentUser User user
    ) {
        return ApiResponse.success(messageUtil, form0581Mapper.toResponse(form0581Service.update(request, id, user)));
    }

    @Operation(summary = "Назначение карты для формы 058-1", description = "Позволяет назначить карту (Card174/175/205) для существующей формы 058-1.")
    @PostMapping("/assign-card")
    public ApiResponse<Form0581Response> assignCard(@Valid @RequestBody Form0581AssignCardRequest request, @CurrentUser User user) {
        return ApiResponse.success(messageUtil, form0581Mapper.toResponse(form0581Facade.assignCard(request, user)));
    }

    @Operation(summary = "Изменение организации получателя формы 058-1", description = "Позволяет изменить только организацию, получившую форму 058-1, без изменения других данных формы.")
    @PatchMapping("/{formId}/change-received-org/{orgId}")
    public ApiResponse<?> changeReceivedOrg(
            @PathVariable Long formId, @PathVariable Long orgId
    ) {
        return ApiResponse.success(messageUtil, form0581Mapper.toResponse(form0581Service.changeReceivedOrg(formId, orgId)));
    }

    @Operation(summary = "Отмена формы 058-1", description = "Позволяет отменить (отклонить) форму 058-1 по её идентификатору с указанием причины.")
    @PatchMapping("/{formId}/cancel")
    public ApiResponse<?> cancel(
            @PathVariable Long formId,
            @RequestBody @Valid Form0581CancelRequest request,
            @CurrentUser User currentUser
    ) {
        return ApiResponse.success(
                messageUtil,
                form0581Mapper.toResponse(form0581Facade.cancel(formId, request.getCancelReason(), currentUser))
        );
    }

    @Operation(summary = "Подтверждение формы 058-1", description = "Позволяет утвердить (закрыть) форму 058-1.")
    @PatchMapping("/{formId}/approve")
    public ApiResponse<?> approve(@PathVariable Long formId, @CurrentUser User user) {
        return ApiResponse.success(messageUtil, form0581Mapper.toResponse(form0581Service.approveForm(formId, user)));
    }

    @Operation(summary = "Отклонение формы 058-1", description = "Позволяет отклонить форму 058-1 с указанием причин.")
    @PutMapping("/{formId}/not-approve")
    public ApiResponse<?> notApprove(
            @PathVariable Long formId,
            @RequestBody Form0581NotApproveRequest notApprove,
            @CurrentUser User user
    ) {
        return ApiResponse.success(messageUtil, form0581Mapper.toResponse(form0581Service.notApprove(formId, user, notApprove)));
    }

    @Operation(
            summary = "Подтверждение диагноза в форме 058-1",
            description = """
                        Позволяет подтвердить диагноз, указанный в форме 058-1.
                        При необходимости можно изменить диагноз перед подтверждением.
                        Если новый диагноз не передан, будет подтверждён существующий.
                    """
    )
    @PutMapping("/{formId}/approve-diagnosis")
    public ApiResponse<Form0581Response> approveDiagnosis(
            @PathVariable Long formId,
            @Valid @RequestBody Form0581ApproveDiagnosisRequest request,
            @RequestHeader(name = AUTHORIZATION) String token,
            @CurrentUser User currentUser
    ) {
        Form0581 form = form0581Service.approveDiagnosis(formId, request, token, currentUser);
        return ApiResponse.success(messageUtil, form0581Mapper.toResponse(form));
    }

    @Operation(summary = "Удаление формы 058-1 по ID", description = "Удаляет форму 058-1 по заданному идентификатору.")
    @PreAuthorize("hasRole('ROLE_isemid_super_admin')")
    @DeleteMapping("/{formId}")
    public ApiResponse<?> deleteById(@PathVariable Long formId) {
        form0581Service.deleteById(formId);
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
        return ApiResponse.success(messageUtil, form0581Service.getConfirmationStats(mkb10Codes, regionCodes, fromDate, toDate, locale, currentUser));
    }

    @GetMapping("/by-card/{cardId}")
    public ApiResponse<Form0581Response> getByCardId(@PathVariable Long cardId) {
        Form0581 form0581 = form0581Service.getByCardId(cardId);
        return ApiResponse.success(messageUtil, form0581Mapper.toResponse(form0581));
    }

    @Operation(summary = "Форма 058-1'га biriktirilgan kartalar", description = "Berilgan forma 058-1 ID'si bo'yicha unga biriktirilgan barcha kartalarni qaytaradi.")
    @GetMapping("/{id}/cards")
    public ApiResponse<List<CardResponse>> getCards(@PathVariable long id) {
        Form0581 form0581 = form0581Service.getById(id);
        List<CardResponse> cards = form0581.getCards().stream()
                .map(cardMapper::toCardResponse)
                .toList();
        return ApiResponse.success(messageUtil, cards);
    }
}
