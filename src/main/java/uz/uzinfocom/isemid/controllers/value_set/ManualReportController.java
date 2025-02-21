package uz.uzinfocom.isemid.controllers.value_set;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
}
