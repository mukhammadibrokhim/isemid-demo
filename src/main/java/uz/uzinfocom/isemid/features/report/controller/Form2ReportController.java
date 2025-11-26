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
import uz.uzinfocom.isemid.features.report.dtos.request.Form2ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.response.Form2ReportResponse;
import uz.uzinfocom.isemid.features.report.mapper.Form2ReportMapper;
import uz.uzinfocom.isemid.features.report.model.Form2Report;
import uz.uzinfocom.isemid.features.report.service.Form2ReportService;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;

import java.time.LocalDate;

import static org.springframework.security.web.util.UrlUtils.buildRequestUrl;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;

@RestController
@RequestMapping(ApiPaths.Report.FORM_2)
@Tag(name = "Report Forms 2")
@RequiredArgsConstructor
public class Form2ReportController {
    private final Form2ReportService form2ReportService;
    private final Form2ReportMapper mapper;
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
        Page<Form2Report> paged = form2ReportService.getReport(page, size, fromDate, toDate, stateCode, cityCode, currentUser);
        return PagedResponse.build(page, paged.map(mapper::toResponse), buildRequestUrl(request), messageUtil);
    }

    @PostMapping
    public ApiResponse<Form2ReportResponse> createForm7Report(
            @RequestBody Form2ReportRequest request
    ) {
        Form2Report report = form2ReportService.create(request);
        return ApiResponse.success(messageUtil, mapper.toResponse(report));
    }

    @GetMapping("/{id}")
    public ApiResponse<Form2ReportResponse> getForm7Report(@PathVariable Long id) {
        return ApiResponse.success(messageUtil, mapper.toResponse(form2ReportService.getById(id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<Form2ReportResponse> updateForm7Report(
            @PathVariable Long id,
            @RequestBody Form2ReportRequest request
    ) {
        return ApiResponse.success(messageUtil, mapper.toResponse(form2ReportService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteForm7Report(@PathVariable Long id) {
        form2ReportService.delete(id);
        return ApiResponse.deleteSuccess(messageUtil);
    }
}
