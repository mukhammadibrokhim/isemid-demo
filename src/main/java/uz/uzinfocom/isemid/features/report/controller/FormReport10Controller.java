package uz.uzinfocom.isemid.features.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.report.service.FormReport10Service;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;

import java.time.LocalDate;

@RestController
@RequestMapping(ApiPaths.Report.FORM10)
@Tag(name = "Report Forms")
@RequiredArgsConstructor
public class FormReport10Controller {
    private final FormReport10Service service;
    private final MessageUtil messageUtil;

    @GetMapping
    public ApiResponse<?> generate(
            @CurrentUser User currentUser,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String parentCode
    ) {
        return ApiResponse.success(messageUtil, service.generate(currentUser, startDate, endDate, parentCode));
    }
}
