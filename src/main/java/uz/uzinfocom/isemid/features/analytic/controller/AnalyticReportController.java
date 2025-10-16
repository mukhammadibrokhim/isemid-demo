package uz.uzinfocom.isemid.features.analytic.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.analytic.dto.request.AnalyticReportRequest;
import uz.uzinfocom.isemid.features.analytic.dto.response.AnalyticReportResponse;
import uz.uzinfocom.isemid.features.analytic.mapper.AnalyticReportMapper;
import uz.uzinfocom.isemid.features.analytic.model.AnalyticReport;
import uz.uzinfocom.isemid.features.analytic.service.AnalyticReportService;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;

import java.time.LocalDate;

import static org.springframework.security.web.util.UrlUtils.buildRequestUrl;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;

@RestController
@RequestMapping(ApiPaths.AnalyticReport.ROOT)
@Tag(name = "Analytic Report")
@RequiredArgsConstructor
public class AnalyticReportController {
    private final AnalyticReportService service;
    private final MessageUtil messageUtil;
    private final AnalyticReportMapper mapper;

    @GetMapping
    public PagedResponse<?> getAnalyticReport(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) String mkb10Code,
            @CurrentUser User user,
            HttpServletRequest request
    ) {
        Page<AnalyticReport> paged = service.getAnalyticReport(page, size, fromDate, toDate, regionCode, mkb10Code, user);
        return PagedResponse.build(page, paged.map(mapper::toResponse), buildRequestUrl(request), messageUtil);
    }

    @PostMapping
    public ApiResponse<AnalyticReportResponse> create(@Valid @RequestBody AnalyticReportRequest request) {
        return ApiResponse.success(messageUtil, mapper.toResponse(service.create(request)));
    }

    @GetMapping("/{id}")
    public ApiResponse<AnalyticReportResponse> getOne(@PathVariable Long id) {
        return ApiResponse.success(messageUtil, mapper.toResponse(service.getOne(id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<AnalyticReportResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody AnalyticReportRequest request) {
        return ApiResponse.success(messageUtil, mapper.toResponse(service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<AnalyticReportResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(messageUtil, null);
    }
}
