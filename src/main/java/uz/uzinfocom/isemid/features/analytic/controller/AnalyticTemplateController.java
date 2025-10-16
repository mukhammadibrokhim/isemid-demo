package uz.uzinfocom.isemid.features.analytic.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.analytic.dto.request.AnalyticTemplateRequest;
import uz.uzinfocom.isemid.features.analytic.dto.response.AnalyticTemplateResponse;
import uz.uzinfocom.isemid.features.analytic.mapper.AnalyticTemplateMapper;
import uz.uzinfocom.isemid.features.analytic.service.AnalyticTemplateService;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.AnalyticTemplate.ROOT)
@RequiredArgsConstructor
public class AnalyticTemplateController {
    private final AnalyticTemplateService service;
    private final MessageUtil messageUtil;
    private final AnalyticTemplateMapper mapper;

    @GetMapping
    public ApiResponse<List<AnalyticTemplateResponse>> getAll(@CurrentUser User user) {
        List<AnalyticTemplateResponse> result = service.getAll(user)
                .stream()
                .map(mapper::toResponse)
                .toList();
        return ApiResponse.success(messageUtil, result);
    }

    @PostMapping
    public ApiResponse<AnalyticTemplateResponse> create(@RequestBody AnalyticTemplateRequest request,
                                                        @CurrentUser User user) {
        return ApiResponse.success(
                messageUtil,
                mapper.toResponse(service.create(request, user)));
    }

    @PutMapping("/{id}")
    public ApiResponse<AnalyticTemplateResponse> update(@PathVariable Long id,
                                                        @RequestBody AnalyticTemplateRequest request,
                                                        @CurrentUser User user) {
        return ApiResponse.success(messageUtil,
                mapper.toResponse(service.update(id, mapper.toEntity(request), user))
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id, @CurrentUser User user) {
        service.delete(id, user);
        return ApiResponse.deleteSuccess(messageUtil);
    }
}
