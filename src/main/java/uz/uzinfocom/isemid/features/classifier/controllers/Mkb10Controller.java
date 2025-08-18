package uz.uzinfocom.isemid.features.classifier.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.PagedResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.Mkb10Request;
import uz.uzinfocom.isemid.features.classifier.mappers.Mkb10Mapper;
import uz.uzinfocom.isemid.features.classifier.models.Mkb10;
import uz.uzinfocom.isemid.features.classifier.services.Mkb10Service;

import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_PAGE;
import static uz.uzinfocom.isemid.commons.constants.PaginationConstants.DEFAULT_SIZE;
import static uz.uzinfocom.isemid.commons.utils.HttpUtil.getRequestUrl;

@RestController
@RequestMapping(ApiPaths.Mkb10.ROOT)
@Tag(name = "Mkb10")
@RequiredArgsConstructor
public class Mkb10Controller {
    private final Mkb10Service mkb10Service;
    private final MessageUtil messageUtil;
    private final Mkb10Mapper mapper;

    @Operation(
            summary = "Получить все записи МКБ-10 с фильтром и пагинацией",
            description = """
                    Возвращает постраничный список записей МКБ-10 с возможностью фильтрации по коду, родителю и названиям на разных языках.
                    Параметры:
                    - page: номер страницы (по умолчанию 0)
                    - size: размер страницы (по умолчанию 20)
                    - mkbCode: код МКБ
                    - parentId: ID родительской записи
                    - nameUz, nameUzCyril, nameRu, nameEn: названия на соответствующих языках
                    """,
            parameters = {
                    @Parameter(name = "page", description = "Номер страницы"),
                    @Parameter(name = "size", description = "Размер страницы"),
                    @Parameter(name = "mkbCode", description = "Код МКБ"),
                    @Parameter(name = "parentId", description = "ID родительской записи"),
                    @Parameter(name = "nameUz", description = "Название на узбекском"),
                    @Parameter(name = "nameUzCyril", description = "Название на узбекском кириллицей"),
                    @Parameter(name = "nameRu", description = "Название на русском"),
                    @Parameter(name = "nameEn", description = "Название на английском")
            }
    )
    @GetMapping("/all")
    public PagedResponse<?> getAllByFilter(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_SIZE) int size,
            @RequestParam(required = false) String mkbCode,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) String nameUz,
            @RequestParam(required = false) String nameUzCyril,
            @RequestParam(required = false) String nameRu,
            @RequestParam(required = false) String nameEn,
            HttpServletRequest request
    ) {
        Page<Mkb10> mkb10Page = mkb10Service.filter(page, size, mkbCode, parentId, nameUz, nameUzCyril, nameRu, nameEn);
        return PagedResponse.build(page, mkb10Page.map(mapper::toResponse), getRequestUrl(request), messageUtil);
    }

    @Operation(
            summary = "Получить все записи МКБ-10 (без пагинации)",
            description = "Возвращает все записи МКБ-10 или по родителю с возможностью поискового запроса.",
            parameters = {
                    @Parameter(name = "parentId", description = "ID родительской записи (опционально)"),
                    @Parameter(name = "search", description = "Поисковый запрос")})
    @GetMapping
    public ApiResponse<?> getMkb10s(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(messageUtil, mkb10Service.getAllMkb10s(parentId, search));
    }

    @Operation(
            summary = "Получить запись МКБ-10 по ID",
            description = "Возвращает запись МКБ-10 по уникальному идентификатору.",
            parameters = {
                    @Parameter(name = "id", description = "ID записи", required = true)
            }
    )
    @GetMapping("/{id}")
    public ApiResponse<?> getById(@PathVariable long id) {
        return ApiResponse.success(messageUtil, mapper.toResponse(mkb10Service.getById(id)));
    }

    @Operation(
            summary = "Обновить запись МКБ-10 по ID",
            description = "Обновляет существующую запись МКБ-10 с указанным ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID записи для обновления", required = true)
            }
    )
    @PutMapping("/{id}")
    public ApiResponse<?> updateById(@PathVariable long id, @RequestBody Mkb10Request request) {
        return ApiResponse.success(messageUtil, mapper.toResponse(mkb10Service.update(id, request)));
    }

    @Operation(
            summary = "Удалить запись МКБ-10 по ID",
            description = "Удаляет запись МКБ-10 с указанным ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID записи для удаления", required = true)
            }
    )
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteById(@PathVariable long id) {
        mkb10Service.delete(id);
        return ApiResponse.deleteSuccess(messageUtil);
    }
}