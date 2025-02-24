package uz.uzinfocom.isemid.controllers.value_set;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.models.payload.requests.value_set.ReqManualReport;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponsePage;
import uz.uzinfocom.isemid.services.value_set.ManualReportService;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.MANUAL_REPORT;
import static uz.uzinfocom.isemid.utils.AppConstant.PAGE;
import static uz.uzinfocom.isemid.utils.AppConstant.SIZE;
import static uz.uzinfocom.isemid.utils.CommonUtils.getFullURL;

@RestController
@RequestMapping(MANUAL_REPORT)
@Tag(name = "Manual Report")
@RequiredArgsConstructor
public class ManualReportController {

    final ManualReportService manualReportService;

    @GetMapping("/all")
    public ApiResponsePage getAll(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = SIZE) int size,
            @RequestParam(required = false) String shortName,
            @RequestParam(required = false) String nameUz,
            @RequestParam(required = false) String nameUzCyril,
            @RequestParam(required = false) String nameRu,
            @RequestParam(required = false) String nameKaa,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Boolean includeTotal,
            HttpServletRequest request
    ) {
        return manualReportService.getAllByFilter(page, size, shortName, nameUz, nameUzCyril, nameRu, nameKaa, code, includeTotal, getFullURL(request));
    }

    @PostMapping
    public ApiResponse<?> post(@Valid @RequestBody ReqManualReport reqManualReport) {
        return manualReportService.create(reqManualReport);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> put(@PathVariable Long id, @RequestBody ReqManualReport reqManualReport) {
        return manualReportService.put(id, reqManualReport);
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
