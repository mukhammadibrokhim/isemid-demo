package uz.uzinfocom.isemid.controllers.value_set;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.entities.User;
import uz.uzinfocom.isemid.models.entities.annotations.CurrentUser;
import uz.uzinfocom.isemid.models.payload.requests.ReqMkb10;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.services.value_set.Mkb10Service;

import java.util.UUID;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.MKB10;
import static uz.uzinfocom.isemid.utils.AppConstant.PAGE;
import static uz.uzinfocom.isemid.utils.AppConstant.SIZE;
import static uz.uzinfocom.isemid.utils.CommonUtils.getFullURL;

@RestController
@RequestMapping(MKB10)
@Tag(name = "Mkb10")
@RequiredArgsConstructor
public class Mkb10Controller {

    final Mkb10Service mkb10Service;

    @GetMapping("/all")
    public ApiResponsePage getAllByFilter(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            @RequestHeader(name = "Organization-id") UUID organizationId,
            @RequestParam(required = false) String mkbCode,
            @RequestParam(required = false) String nameUz,
            @RequestParam(required = false) String nameUzCyril,
            @RequestParam(required = false) String nameRu,
            @RequestParam(required = false) String nameEn,
            HttpServletRequest request
    ) {
        return mkb10Service.getMkb10ByFilter(page, size, mkbCode, nameUz, nameUzCyril, nameRu, nameEn, getFullURL(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getById(@PathVariable long id) {
        return mkb10Service.getById(id);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateById(@PathVariable long id, @RequestBody ReqMkb10 reqMkb10, @CurrentUser User user) {
        return mkb10Service.updateById(id, reqMkb10, user);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteById(@PathVariable long id) {
        return mkb10Service.deleteById(id);
    }

    @GetMapping("/parents")
    public ApiResponse<?> getMkb10ByParent() {
        return mkb10Service.getParentWithChildren();
    }
}
