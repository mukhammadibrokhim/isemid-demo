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
import uz.uzinfocom.isemid.features.report.dtos.request.Form3_2ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.response.Form3_2ReportResponse;
import uz.uzinfocom.isemid.features.report.mapper.Form3_2ReportMapper;
import uz.uzinfocom.isemid.features.report.model.Form3_2Report;
import uz.uzinfocom.isemid.features.report.service.Form3_2ReportService;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;

import java.time.LocalDate;

import static org.springframework.security.web.util.UrlUtils.buildRequestUrl;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;

@RestController
@RequestMapping(ApiPaths.Report.FORM_3_2)
@Tag(name = "Report Forms")
@RequiredArgsConstructor
public class Form3_2ReportController {
    private final Form3_2ReportService form3_2ReportService;
    private final Form3_2ReportMapper form3_2ReportMapper;
    private final MessageUtil messageUtil;

    @GetMapping("/all")
    public PagedResponse<?> getAll(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam LocalDate fromDate,
            @RequestParam LocalDate toDate,
            @RequestParam String stateCode,
            @RequestParam String cityCode,
            @CurrentUser User currentUser,
            HttpServletRequest request
    ) {
        Page<Form3_2Report> paged = form3_2ReportService.getReport(page, size, fromDate, toDate, stateCode, cityCode, currentUser);
        return PagedResponse.build(page, paged.map(form3_2ReportMapper::toResponse), buildRequestUrl(request), messageUtil);
    }

    @PostMapping
    public ApiResponse<Form3_2ReportResponse> createForm7Report(
            @RequestBody Form3_2ReportRequest request
    ) {
        Form3_2Report form3_2Report = form3_2ReportService.create(request);
        return ApiResponse.success(messageUtil, form3_2ReportMapper.toResponse(form3_2Report));
    }

    @GetMapping("/{id}")
    public ApiResponse<Form3_2ReportResponse> getForm7Report(@PathVariable Long id) {
        return ApiResponse.success(messageUtil, form3_2ReportMapper.toResponse(form3_2ReportService.getById(id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<Form3_2ReportResponse> updateForm7Report(
            @PathVariable Long id,
            @RequestBody Form3_2ReportRequest request
    ) {
        return ApiResponse.success(messageUtil, form3_2ReportMapper.toResponse(form3_2ReportService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteForm7Report(@PathVariable Long id) {
        form3_2ReportService.delete(id);
        return ApiResponse.deleteSuccess(messageUtil);
    }
}
