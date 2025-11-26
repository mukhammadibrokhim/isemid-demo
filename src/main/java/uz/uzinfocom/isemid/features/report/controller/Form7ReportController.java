package uz.uzinfocom.isemid.features.report.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
import uz.uzinfocom.isemid.features.report.dtos.response.Form7ReportResponse;
import uz.uzinfocom.isemid.features.report.mapper.Form7ReportMapper;
import uz.uzinfocom.isemid.features.report.model.Form7Report;
import uz.uzinfocom.isemid.features.report.service.Form7ReportService;
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
public class Form7ReportController {
    private final Form7ReportService form7ReportService;
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
        Page<Form7Report> paged = form7ReportService.getReport(page, size, fromDate, toDate, stateCode, cityCode, currentUser);
        return PagedResponse.build(page, paged.map(form7ReportMapper::toResponse), buildRequestUrl(request), messageUtil);
    }

    @PostMapping
    public ApiResponse<Form7ReportResponse> createForm7Report(
            @RequestBody Form7ReportRequest request
    ) {
        Form7Report form7Report = form7ReportService.create(request);
        return ApiResponse.success(messageUtil, form7ReportMapper.toResponse(form7Report));
    }

    @GetMapping("/{id}")
    public ApiResponse<Form7ReportResponse> getForm7Report(@PathVariable Long id) {
        return ApiResponse.success(messageUtil, form7ReportMapper.toResponse(form7ReportService.getById(id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<Form7ReportResponse> updateForm7Report(
            @PathVariable Long id,
            @RequestBody Form7ReportRequest request
    ) {
        return ApiResponse.success(messageUtil, form7ReportMapper.toResponse(form7ReportService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteForm7Report(@PathVariable Long id) {
        form7ReportService.delete(id);
        return ApiResponse.deleteSuccess(messageUtil);
    }

}
