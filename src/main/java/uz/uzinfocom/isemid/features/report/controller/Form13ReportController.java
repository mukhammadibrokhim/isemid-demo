package uz.uzinfocom.isemid.features.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.report.dtos.Form13Dto;
import uz.uzinfocom.isemid.features.report.service.Form13Service;

@RestController
@RequestMapping(ApiPaths.Report.FORM13)
@Tag(name = "Report Forms")
@RequiredArgsConstructor
public class Form13ReportController {
    private final Form13Service form13Service;
    private final MessageUtil messageUtil;

    @GetMapping
    public ApiResponse<Form13Dto> getForm13Report() {

        return ApiResponse.success(messageUtil, null);
    }
}
