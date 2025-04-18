package uz.uzinfocom.isemid.features.classifier.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.ApiResponsePage;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.ManualReportRequest;
import uz.uzinfocom.isemid.features.classifier.services.ManualReportService;

import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;
import static uz.uzinfocom.isemid.commons.utils.HttpUtil.getRequestUrl;

@RestController
@RequestMapping(ApiPaths.ManualReport.ROOT)
@Tag(name = "Manual Report")
@RequiredArgsConstructor
public class ManualReportController {

    final ManualReportService manualReportService;

    @GetMapping("/all")
    public ApiResponsePage getAll(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false) String shortName,
            @RequestParam(required = false) String nameUz,
            @RequestParam(required = false) String nameUzCyril,
            @RequestParam(required = false) String nameRu,
            @RequestParam(required = false) String nameKaa,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Boolean includeTotal,
            HttpServletRequest request
    ) {
        return manualReportService.getAllByFilter(page, size, shortName, nameUz, nameUzCyril, nameRu, nameKaa, code, includeTotal, getRequestUrl(request));
    }

    @PostMapping
    public ApiResponse<?> post(@Valid @RequestBody ManualReportRequest manualReportRequest) {
        return manualReportService.create(manualReportRequest);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> put(@PathVariable Long id, @RequestBody ManualReportRequest manualReportRequest) {
        return manualReportService.put(id, manualReportRequest);
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getOne(@PathVariable Long id) {
        return manualReportService.getById(id);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        return manualReportService.delete(id);
    }
}
