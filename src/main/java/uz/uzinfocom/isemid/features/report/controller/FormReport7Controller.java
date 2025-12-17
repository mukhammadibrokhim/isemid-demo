package uz.uzinfocom.isemid.features.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.response.FormReport7Response;
import uz.uzinfocom.isemid.features.report.mapper.Form7ReportMapper;
import uz.uzinfocom.isemid.features.report.model.FormReport7;
import uz.uzinfocom.isemid.features.report.service.FormReport7Service;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;

import java.time.LocalDate;

import static org.springframework.security.web.util.UrlUtils.buildRequestUrl;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;

@RestController
@RequestMapping(ApiPaths.Report.FORM7)
@Tag(name = "Report Forms 7")
@RequiredArgsConstructor
public class FormReport7Controller {
    private final FormReport7Service formReport7Service;
    private final Form7ReportMapper form7ReportMapper;
    private final MessageUtil messageUtil;

    @GetMapping("/all")
    public PagedResponse<?> getAll(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String stateCode,
            @RequestParam(required = false) String cityCode,
            @CurrentUser User currentUser,
            HttpServletRequest request
    ) {
        Page<FormReport7> paged = formReport7Service.getReport(page, size, fromDate, toDate, stateCode, cityCode, currentUser);
        return PagedResponse.build(page, paged.map(form7ReportMapper::toResponse), buildRequestUrl(request), messageUtil);
    }

    @PostMapping
    public ApiResponse<FormReport7Response> createForm7Report(
            @CurrentUser User currentUser
    ) {
        FormReport7 formReport7 = formReport7Service.create(currentUser);
        return ApiResponse.success(messageUtil, form7ReportMapper.toResponse(formReport7));
    }

    @GetMapping("/{id}")
    public ApiResponse<FormReport7Response> getForm7Report(@PathVariable Long id) {
        return ApiResponse.success(messageUtil, form7ReportMapper.toResponse(formReport7Service.getById(id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<FormReport7Response> updateForm7Report(
            @PathVariable Long id,
            @RequestBody Form7ReportRequest request
    ) {
        return ApiResponse.success(messageUtil, form7ReportMapper.toResponse(formReport7Service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteForm7Report(@PathVariable Long id) {
        formReport7Service.delete(id);
        return ApiResponse.deleteSuccess(messageUtil);
    }

}
