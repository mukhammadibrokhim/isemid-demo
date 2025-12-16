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
import uz.uzinfocom.isemid.features.report.dtos.request.Form3_1ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.response.FormReport31Response;
import uz.uzinfocom.isemid.features.report.mapper.FormReport31Mapper;
import uz.uzinfocom.isemid.features.report.model.FormReport31;
import uz.uzinfocom.isemid.features.report.service.FormReport31Service;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;

import java.time.LocalDate;

import static org.springframework.security.web.util.UrlUtils.buildRequestUrl;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;

@RestController
@RequestMapping(ApiPaths.Report.FORM_3_1)
@Tag(name = "Report Forms 3.1")
@RequiredArgsConstructor
public class FormReport31Controller {
    private final FormReport31Service form3_1ReportService;
    private final FormReport31Mapper form3_1ReportMapper;
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
        Page<FormReport31> paged = form3_1ReportService.getReport(page, size, fromDate, toDate, stateCode, cityCode, currentUser);
        return PagedResponse.build(page, paged.map(form3_1ReportMapper::toResponse), buildRequestUrl(request), messageUtil);
    }

    @PostMapping
    public ApiResponse<FormReport31Response> createForm7Report(
            @RequestBody Form3_1ReportRequest request
    ) {
        FormReport31 formReport31 = form3_1ReportService.create(request);
        return ApiResponse.success(messageUtil, form3_1ReportMapper.toResponse(formReport31));
    }

    @GetMapping("/{id}")
    public ApiResponse<FormReport31Response> getForm7Report(@PathVariable Long id) {
        return ApiResponse.success(messageUtil, form3_1ReportMapper.toResponse(form3_1ReportService.getById(id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<FormReport31Response> updateForm7Report(
            @PathVariable Long id,
            @RequestBody Form7ReportRequest request
    ) {
        return ApiResponse.success(messageUtil, form3_1ReportMapper.toResponse(form3_1ReportService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteForm7Report(@PathVariable Long id) {
        form3_1ReportService.delete(id);
        return ApiResponse.deleteSuccess(messageUtil);
    }
}
