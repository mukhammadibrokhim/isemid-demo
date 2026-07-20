package uz.uzinfocom.isemid.features.form129.controllers;

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
import uz.uzinfocom.isemid.features.form058.models.enums.FilterType;
import uz.uzinfocom.isemid.features.form129.dtos.requests.Form129FilterRequest;
import uz.uzinfocom.isemid.features.form129.dtos.requests.Form129Request;
import uz.uzinfocom.isemid.features.form129.dtos.responses.Form129Response;
import uz.uzinfocom.isemid.features.form129.mappers.Form129Mapper;
import uz.uzinfocom.isemid.features.form129.models.Form129;
import uz.uzinfocom.isemid.features.form129.services.Form129Service;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;

import static org.springframework.http.HttpStatus.CREATED;
import static uz.uzinfocom.isemid.commons.utils.RequestUrlUtils.getRequestUrl;

@RestController
@RequestMapping(ApiPaths.Form129.ROOT)
@Tag(name = "Form 129", description = "API для работы с формой 129 (лабораторная справка)")
@RequiredArgsConstructor
public class Form129Controller {
    private final Form129Service form129Service;
    private final MessageUtil messageUtil;
    private final Form129Mapper form129Mapper;

    @Operation(summary = "Создание формы 129", description = "Создаёт новую форму 129 с данными пациента и лабораторными результатами.")
    @PostMapping
    @ResponseStatus(CREATED)
    public ApiResponse<Form129Response> create(
            @Valid @RequestBody Form129Request request,
            @CurrentUser User currentUser
    ) {
        return ApiResponse.success(messageUtil, form129Mapper.toResponse(form129Service.create(request, currentUser)));
    }

    @GetMapping("/all")
    public PagedResponse<Form129Response> getAll(
            @Valid @ModelAttribute @ParameterObject Form129FilterRequest filterRequest,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        Page<Form129> page = form129Service.getAll(filterRequest, user, FilterType.SENDER);
        return PagedResponse.build(filterRequest.page(), page.map(form129Mapper::toResponse), getRequestUrl(request), messageUtil);
    }

    @GetMapping("/received/all")
    public PagedResponse<Form129Response> getReceivedAll(
            @Valid @ModelAttribute @ParameterObject Form129FilterRequest filterRequest,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        Page<Form129> page = form129Service.getAll(filterRequest, user, FilterType.RECEIVER);
        return PagedResponse.build(filterRequest.page(), page.map(form129Mapper::toResponse), getRequestUrl(request), messageUtil);
    }

    @Operation(summary = "Получение формы 129 по ID", description = "Возвращает данные конкретной формы 129 по заданному идентификатору.")
    @GetMapping("/{id}")
    public ApiResponse<Form129Response> get(@PathVariable long id) {
        return ApiResponse.success(messageUtil, form129Mapper.toResponse(form129Service.getById(id)));
    }

    @Operation(summary = "Обновление формы 129", description = "Обновляет данные существующей формы 129 по заданному идентификатору.")
    @PutMapping("/{id}")
    public ApiResponse<Form129Response> update(
            @RequestBody Form129Request request,
            @PathVariable Long id,
            @CurrentUser User user
    ) {
        return ApiResponse.success(messageUtil, form129Mapper.toResponse(form129Service.update(request, id, user)));
    }

    @Operation(summary = "Удаление формы 129 по ID", description = "Удаляет форму 129 по заданному идентификатору.")
    @PreAuthorize("hasRole('ROLE_isemid_super_admin')")
    @DeleteMapping("/{formId}")
    public ApiResponse<?> deleteById(@PathVariable Long formId) {
        form129Service.deleteById(formId);
        return ApiResponse.deleteSuccess(messageUtil);
    }
}
