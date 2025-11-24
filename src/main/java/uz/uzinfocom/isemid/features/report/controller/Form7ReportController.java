package uz.uzinfocom.isemid.features.report.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.response.Form7ReportResponse;
import uz.uzinfocom.isemid.features.report.mapper.Form7ReportMapper;
import uz.uzinfocom.isemid.features.report.model.Form7Report;
import uz.uzinfocom.isemid.features.report.service.Form7ReportService;

@RestController
@RequestMapping(ApiPaths.Report.FORM7)
@Tag(name = "Report Forms")
@RequiredArgsConstructor
public class Form7ReportController {
    private final Form7ReportService form7ReportService;
    private final Form7ReportMapper form7ReportMapper;
    private final MessageUtil messageUtil;

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
