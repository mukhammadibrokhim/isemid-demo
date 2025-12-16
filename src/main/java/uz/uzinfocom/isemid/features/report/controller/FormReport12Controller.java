package uz.uzinfocom.isemid.features.report.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.report.dtos.Report12Dto;
import uz.uzinfocom.isemid.features.report.mapper.FormReport12Mapper;
import uz.uzinfocom.isemid.features.report.model.FormReport12;
import uz.uzinfocom.isemid.features.report.repository.FormReport12Repository;
import uz.uzinfocom.isemid.features.report.service.FormReport12Service;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;

import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;
import static uz.uzinfocom.isemid.commons.utils.HttpUtil.getRequestUrl;

@RestController
@RequestMapping(ApiPaths.Report.FORM12_7)
@Tag(name = "Report Forms")
@RequiredArgsConstructor
public class FormReport12Controller {
    private final FormReport12Service report12Service;
    private final FormReport12Repository report12Repository;
    private final FormReport12Mapper report12Mapper;
    private final MessageUtil messageUtil;

    @GetMapping("/all")
    public PagedResponse<?> getAllReport12(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false) String regionName,
            @CurrentUser User user,
            HttpServletRequest request
    ) {

        Page<FormReport12> data = report12Service.filter(page, size, regionName, user);

        return PagedResponse.build(
                page,
                data.map(report12Mapper::toDto),
                getRequestUrl(request),
                messageUtil
        );
    }

    @PostMapping
    public Report12Dto create(@RequestBody Report12Dto dto) {
        FormReport12 created = report12Service.create(dto);
        return report12Mapper.toDto(created);
    }

    @PutMapping("/{id}")
    public Report12Dto update(@PathVariable Long id, @RequestBody Report12Dto dto) {
        dto.setId(id);
        FormReport12 updated = report12Service.update(dto);
        return report12Mapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        report12Service.delete(id);
    }

    @GetMapping("/{id}")
    public Report12Dto getById(@PathVariable Long id) {
        FormReport12 entity = report12Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report12 not found with id " + id));
        return report12Mapper.toDto(entity);
    }
}
