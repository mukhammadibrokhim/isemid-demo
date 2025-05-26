package uz.uzinfocom.isemid.features.form058.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.ApiResponsePage;
import uz.uzinfocom.isemid.features.form058.dtos.requests.*;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058Response;
import uz.uzinfocom.isemid.features.form058.enums.FilterType;
import uz.uzinfocom.isemid.features.form058.services.Form058Service;
import uz.uzinfocom.isemid.features.user.annotation.CurrentUser;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

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

    @Operation(
            summary = "Создание формы 058",
            description = "Создаёт новую форму 058 с данными пациента и другой необходимой информацией.")
    @PostMapping
    @ResponseStatus(CREATED)
    public ApiResponse<Form058Response> create(
            @Valid @RequestBody Form058Request form058Request,
            @CurrentUser User user) {
        return form058Service.create(form058Request, user);
    }

    @Operation(
            summary = "Получение всех форм 058",
            description = "Возвращает постраничный список всех форм 058 с возможностью фильтрации по дате.")
    @GetMapping("/all")
    public ApiResponsePage getAll(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        return form058Service.getAllFilteredForm058(page, size, fromDate, toDate, user, getRequestUrl(request), FilterType.ALL);
    }

    @Operation(
            summary = "Получение всех полученных форм 058",
            description = "Возвращает постраничный список всех форм 058, которые были получены, с возможностью фильтрации по дате.")
    @GetMapping("/received/all")
    public ApiResponsePage getReceivedAll(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        return form058Service.getAllFilteredForm058(page, size, fromDate, toDate, user, getRequestUrl(request), FilterType.RECEIVED);
    }

    @Operation(
            summary = "Получение формы 058 по ID",
            description = "Возвращает данные конкретной формы 058 по заданному идентификатору.")
    @GetMapping("/{id}")
    public ApiResponse<Form058Response> get(@PathVariable long id) {
        return form058Service.getById(id);
    }

    @Operation(
            summary = "Обновление формы 058",
            description = "Обновляет данные существующей формы 058 по заданному идентификатору.")
    @PutMapping("/{id}")
    public ApiResponse<Form058Response> update(
            @RequestBody Form058Request request,
            @PathVariable Long id,
            @CurrentUser User user
    ) {
        return form058Service.update(request, id, user);
    }

    @Operation(
            summary = "Назначение карты для формы 058",
            description = "Позволяет назначить карту (Card) для существующей формы 058.")
    @PostMapping("/assign-card")
    public ApiResponse<Form058Response> assignCard(@Valid @RequestBody Form058AssignCardRequest request, @CurrentUser User user) {
        return form058Service.assignCard(request, user);
    }

    @Operation(
            summary = "Перевод формы 058 в другую организацию",
            description = "Позволяет изменить организацию, в которую была получена форма 058.")
    @PutMapping("/{formId}/change-received-org")
    public ApiResponse<?> changeReceivedOrg(
            @PathVariable Long formId,
            @Valid @RequestBody Form058UpdateReceivedOrg updateReceivedOrg
    ) {
        return form058Service.changeReceivedOrd(formId, updateReceivedOrg);
    }

    @Operation(
            summary = "Отмена формы 058",
            description = "Позволяет отменить (отклонить) форму 058 по её идентификатору.")
    @PatchMapping("/{formId}/cancel")
    public ApiResponse<?> cancel(@PathVariable Long formId) {
        return form058Service.cancel(formId);
    }

    @Operation(
            summary = "Подтверждение формы 058",
            description = "Позволяет утвердить форму 058.")
    @PatchMapping("/{formId}/approve")
    public ApiResponse<?> approve(@PathVariable Long formId) {
        return form058Service.approveForm(formId);
    }

    @Operation(
            summary = "Отклонение формы 058",
            description = "Позволяет отклонить форму 058 с указанием причин.")
    @PutMapping("/{formId}/not-approve")
    public ApiResponse<?> notApprove(@PathVariable Long formId, @RequestBody Form058NotApproveRequest notApprove) {
        return form058Service.notApprove(formId, notApprove);
    }

    @Operation(
            summary = "Изменение диагноза в форме 058",
            description = "Позволяет изменить диагноз, указанный в форме 058.")
    @PutMapping("/{formId}/change-diagnosis")
    public ApiResponse<?> changeDiagnosis(@PathVariable Long formId, @RequestBody Form058ChangeDiagnosisRequest changeDiagnosis) {
        return form058Service.changeDiagnosis(formId, changeDiagnosis);
    }

    @Operation(
            summary = "Удаление формы 058 по ID",
            description = "Удаляет форму 058 по заданному идентификатору.")
    @DeleteMapping("/{formId}")
    public ApiResponse<?> deleteById(@PathVariable Long formId) {
        return form058Service.deleteById(formId);
    }
}

