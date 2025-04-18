package uz.uzinfocom.isemid.features.classifier.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.dtos.ApiResponsePage;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.Mkb10Request;
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

    @GetMapping("/all")
    public ApiResponsePage getAllByFilter(
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
        return mkb10Service.getMkb10ByFilter(page, size, mkbCode, parentId, nameUz, nameUzCyril, nameRu, nameEn, getRequestUrl(request));
    }

    @GetMapping
    public ApiResponse<?> getMkb10s(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) String search
    ) {
        return mkb10Service.getAllMkb10s(parentId, search);
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getById(@PathVariable long id) {
        return mkb10Service.getById(id);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateById(@PathVariable long id, @RequestBody Mkb10Request request) {
        return mkb10Service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteById(@PathVariable long id) {
        return mkb10Service.delete(id);
    }
}
