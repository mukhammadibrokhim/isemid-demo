package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.Form058;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.annotations.CurrentUser;
import uz.uzinfocom.isemid.models.entities.enums.FilterType;
import uz.uzinfocom.isemid.models.payload.requests.form.ReqForm058;
import uz.uzinfocom.isemid.models.payload.requests.form.ReqForm058AssignCard;
import uz.uzinfocom.isemid.models.payload.requests.form.ReqForm058OrgUpdate;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.services.Form058Service;

import static org.springframework.http.HttpStatus.CREATED;
import static uz.uzinfocom.isemid.utils.ApiUrlConstant.FORM0058;
import static uz.uzinfocom.isemid.utils.AppConstant.PAGE;
import static uz.uzinfocom.isemid.utils.AppConstant.SIZE;
import static uz.uzinfocom.isemid.utils.CommonUtils.getFullURL;

@RestController
@RequestMapping(FORM0058)
@Tag(name = "Form 058", description = "API для работы с формой 058")
@RequiredArgsConstructor
public class Form058Controller {

    final Form058Service form058Service;

    @Operation(
            summary = "Создание формы 058",
            description = "Создаёт новую форму 058 с данными пациента и другой необходимой информацией.")
    @PostMapping
    @ResponseStatus(CREATED)
    public ApiResponse<Form058> create(
            @Valid @RequestBody ReqForm058 reqForm058,
            @CurrentUser User user) {
        return form058Service.createForm058(reqForm058, user);
    }

    @GetMapping("/all")
    public ApiResponsePage getAll(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        return form058Service.getFilteredForm058(page, size, user, getFullURL(request), FilterType.ALL);
    }

    @GetMapping("/received/all")
    public ApiResponsePage getReceivedAll(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        return form058Service.getFilteredForm058(page, size, user, getFullURL(request), FilterType.RECEIVED);
    }

    @GetMapping("/{id}")
    public ApiResponse<Form058> get(@PathVariable long id) {
        return form058Service.getById(id);
    }

    @Operation(
            summary = "Обновление формы 058",
            description = "Обновляет существующую форму 058 по заданному идентификатору.")
    @PutMapping("/{id}")
    public ApiResponse<Form058> update(
            @RequestBody ReqForm058 reqForm058,
            @PathVariable Long id,
            @CurrentUser User user
    ) {
        return form058Service.updateForm058(reqForm058, id, user);
    }

    @Operation(summary = "Назначение карты для Form058", description = "Этот эндпоинт назначает карту (Card) для существующей записи Form058. ")
    @PostMapping("/assign-card")
    public ApiResponse<Form058> assignCard(@Valid @RequestBody ReqForm058AssignCard assignCard,@CurrentUser User user) {
        return form058Service.assignCard(assignCard,user);
    }

    @Operation(summary = "Перевод Form058 в другую организацию",
            description = "Этот эндпоинт позволяет перевести форму Form058 в другую организацию. ")
    @PutMapping("/{formId}/change-received-org")
    public ApiResponse<?> changeReceivedOrg(
            @PathVariable Long formId,
            @Valid @RequestBody ReqForm058OrgUpdate reqForm058OrgUpdate
            ) {
        return form058Service.changeReceivedOrd(formId, reqForm058OrgUpdate);
    }

    @PatchMapping("/{formId}/cancel")
    public ApiResponse<?> cancel(@PathVariable Long formId) {
        return form058Service.cancel(formId);
    }

    @PatchMapping("/{formId}/approve")
    public ApiResponse<?> approve(@PathVariable Long formId) {
        return form058Service.approveForm(formId);
    }

    @PatchMapping("/{formId}/not-approve")
    public ApiResponse<?> notApprove(@PathVariable Long formId) {
        return form058Service.notApprove(formId);
    }

    @DeleteMapping("/{formId}")
    public ApiResponse<?> deleteById(@PathVariable Long formId) {
        return form058Service.deleteById(formId);
    }
}

