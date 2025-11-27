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
import uz.uzinfocom.isemid.features.report.mapper.Report12Mapper;
import uz.uzinfocom.isemid.features.report.models.Report12;
import uz.uzinfocom.isemid.features.report.repositories.Report12Repository;
import uz.uzinfocom.isemid.features.report.service.Report12Service;
import uz.uzinfocom.isemid.features.user.models.User;
import uz.uzinfocom.isemid.features.user.models.annotation.CurrentUser;

import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;
import static uz.uzinfocom.isemid.commons.utils.HttpUtil.getRequestUrl;

@RestController
@RequestMapping(ApiPaths.Report.FORM12_7)
@Tag(name = "Report Forms")
@RequiredArgsConstructor
public class Form12_7Controller {
    private final Report12Service report12Service;
    private final Report12Repository report12Repository;
    private final Report12Mapper report12Mapper;
    private final MessageUtil messageUtil;

    @GetMapping("/all")
    public PagedResponse<?> getAllReport12(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false) String regionName,
            @CurrentUser User user,
            HttpServletRequest request
    ) {

        Page<Report12> data = report12Service.filter(page, size, regionName, user);

        return PagedResponse.build(
                page,
                data.map(report12Mapper::toDto),
                getRequestUrl(request),
                messageUtil
        );
    }

    @PostMapping
    public Report12Dto create(@RequestBody Report12Dto dto) {
        Report12 created = report12Service.create(dto);
        return report12Mapper.toDto(created);
    }

    @PutMapping("/{id}")
    public Report12Dto update(@PathVariable Long id, @RequestBody Report12Dto dto) {
        dto.setId(id);
        Report12 updated = report12Service.update(dto);
        return report12Mapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        report12Service.delete(id);
    }

    @GetMapping("/{id}")
    public Report12Dto getById(@PathVariable Long id) {
        Report12 entity = report12Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report12 not found with id " + id));
        return report12Mapper.toDto(entity);
    }
}
