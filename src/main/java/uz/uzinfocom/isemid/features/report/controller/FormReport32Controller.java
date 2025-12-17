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
import uz.uzinfocom.isemid.features.report.dtos.request.Form3_2ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.response.FormReport32Response;
import uz.uzinfocom.isemid.features.report.mapper.FormReport32Mapper;
import uz.uzinfocom.isemid.features.report.model.FormReport32;
import uz.uzinfocom.isemid.features.report.service.FormReport32Service;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;

import java.time.LocalDate;

import static org.springframework.security.web.util.UrlUtils.buildRequestUrl;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;

@RestController
@RequestMapping(ApiPaths.Report.FORM_3_2)
@Tag(name = "Report Forms 3.2")
@RequiredArgsConstructor
public class FormReport32Controller {
    private final FormReport32Service form3_2ReportService;
    private final FormReport32Mapper form3_2ReportMapper;
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
        Page<FormReport32> paged = form3_2ReportService.getReport(page, size, fromDate, toDate, stateCode, cityCode, currentUser);
        return PagedResponse.build(page, paged.map(form3_2ReportMapper::toResponse), buildRequestUrl(request), messageUtil);
    }

    @PostMapping
    public ApiResponse<FormReport32Response> createForm7Report(
            @RequestBody Form3_2ReportRequest request
    ) {
        FormReport32 formReport32 = form3_2ReportService.create(request);
        return ApiResponse.success(messageUtil, form3_2ReportMapper.toResponse(formReport32));
    }

    @GetMapping("/{id}")
    public ApiResponse<FormReport32Response> getForm7Report(@PathVariable Long id) {
        return ApiResponse.success(messageUtil, form3_2ReportMapper.toResponse(form3_2ReportService.getById(id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<FormReport32Response> updateForm7Report(
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
