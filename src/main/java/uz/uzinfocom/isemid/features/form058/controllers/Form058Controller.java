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

    @GetMapping("/all")
    public ApiResponsePage getAll(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        return form058Service.getAllFilteredForm058(page, size, user, getRequestUrl(request), FilterType.ALL);
    }

    @GetMapping("/received/all")
    public ApiResponsePage getReceivedAll(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        return form058Service.getAllFilteredForm058(page, size, user, getRequestUrl(request), FilterType.RECEIVED);
    }

    @GetMapping("/{id}")
    public ApiResponse<Form058Response> get(@PathVariable long id) {
        return form058Service.getById(id);
    }

    @Operation(
            summary = "Обновление формы 058",
            description = "Обновляет существующую форму 058 по заданному идентификатору.")
    @PutMapping("/{id}")
    public ApiResponse<Form058Response> update(
            @RequestBody Form058Request request,
            @PathVariable Long id,
            @CurrentUser User user
    ) {
        return form058Service.update(request, id, user);
    }

    @Operation(summary = "Назначение карты для Form058", description = "Этот эндпоинт назначает карту (Card) для существующей записи Form058. ")
    @PostMapping("/assign-card")
    public ApiResponse<Form058Response> assignCard(@Valid @RequestBody Form058AssignCardRequest request, @CurrentUser User user) {
        return form058Service.assignCard(request, user);
    }

    @Operation(summary = "Перевод Form058 в другую организацию",
            description = "Этот эндпоинт позволяет перевести форму Form058 в другую организацию. ")
    @PutMapping("/{formId}/change-received-org")
    public ApiResponse<?> changeReceivedOrg(
            @PathVariable Long formId,
            @Valid @RequestBody Form058UpdateReceivedOrg updateReceivedOrg
    ) {
        return form058Service.changeReceivedOrd(formId, updateReceivedOrg);
    }

    @PatchMapping("/{formId}/cancel")
    public ApiResponse<?> cancel(@PathVariable Long formId) {
        return form058Service.cancel(formId);
    }

    @PatchMapping("/{formId}/approve")
    public ApiResponse<?> approve(@PathVariable Long formId) {
        return form058Service.approveForm(formId);
    }

    @PutMapping("/{formId}/not-approve")
    public ApiResponse<?> notApprove(@PathVariable Long formId, @RequestBody Form058NotApproveRequest notApprove) {
        return form058Service.notApprove(formId, notApprove);
    }

    @PutMapping("/{formId}/change-diagnosis")
    public ApiResponse<?> changeDiagnosis(@PathVariable Long formId, @RequestBody Form058ChangeDiagnosisRequest changeDiagnosis) {
        return form058Service.changeDiagnosis(formId, changeDiagnosis);
    }

    @DeleteMapping("/{formId}")
    public ApiResponse<?> deleteById(@PathVariable Long formId) {
        return form058Service.deleteById(formId);
    }
}

