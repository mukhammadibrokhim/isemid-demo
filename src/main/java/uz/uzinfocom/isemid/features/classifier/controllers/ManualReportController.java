package uz.uzinfocom.isemid.features.classifier.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
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

    private final ManualReportService manualReportService;

    @Operation(
            summary = "Получить все ручные отчеты",
            description = """
                    Возвращает список всех ручных отчетов с возможностью фильтрации по параметрам:
                    - shortName,
                    - nameUz,
                    - nameUzCyril,
                    - nameRu,
                    - nameKaa,
                    - code,
                    - includeTotal (включать ли общий результат).
                    Поддерживается пагинация с параметрами page и size.
                    """,
            parameters = {
                    @Parameter(name = "page", description = "Номер страницы"),
                    @Parameter(name = "size", description = "Размер страницы"),
                    @Parameter(name = "shortName", description = "Краткое название"),
                    @Parameter(name = "nameUz", description = "Название на узбекском"),
                    @Parameter(name = "nameUzCyril", description = "Название на узбекском кириллицей"),
                    @Parameter(name = "nameRu", description = "Название на русском"),
                    @Parameter(name = "nameKaa", description = "Название на языке Kaa"),
                    @Parameter(name = "code", description = "Код"),
                    @Parameter(name = "includeTotal", description = "Включать ли общий результат")})
    @GetMapping("/all")
    public PagedResponse<?> getAll(
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

    @Operation(
            summary = "Создать новый ручной отчет",
            description = "Создает новый ручной отчет с переданными данными.")
    @PostMapping
    public ApiResponse<?> post(@Valid @RequestBody ManualReportRequest manualReportRequest) {
        return manualReportService.create(manualReportRequest);
    }

    @Operation(
            summary = "Обновить ручной отчет по ID",
            description = "Обновляет существующий ручной отчет с указанным ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID отчета для обновления", required = true)})
    @PutMapping("/{id}")
    public ApiResponse<?> put(@PathVariable Long id, @RequestBody ManualReportRequest manualReportRequest) {
        return manualReportService.update(id, manualReportRequest);
    }

    @Operation(
            summary = "Получить ручной отчет по ID",
            description = "Возвращает ручной отчет по указанному ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID отчета", required = true)
            }
    )
    @GetMapping("/{id}")
    public ApiResponse<?> getOne(@PathVariable Long id) {
        return manualReportService.getById(id);
    }

    @Operation(
            summary = "Удалить ручной отчет по ID",
            description = "Удаляет ручной отчет по указанному ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID отчета для удаления", required = true)
            }
    )
    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        return manualReportService.delete(id);
    }
}
