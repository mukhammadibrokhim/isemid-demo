package uz.uzinfocom.app.integration.dhp.employment.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.integration.dhp.employment.application.DhpEmploymentLookupService;
import uz.uzinfocom.app.integration.dhp.employment.web.dto.EmploymentResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

@Tag(name = "DHP Employment", description = "Сведения о трудовой занятости гражданина из DHP (egov MOL).")
@RestController
@RequestMapping(ApiPaths.Dhp.EMPLOYMENT)
@RequiredArgsConstructor
public class DhpEmploymentController {

    private final DhpEmploymentLookupService lookupService;

    @Operation(
            summary = "Получить места работы гражданина по ПИНФЛ",
            description = "Запрашивает у DHP (egov MOL, server-to-server) данные о занятости гражданина по ПИНФЛ "
                    + "и возвращает их в структуре ISEMID. Если сведений нет — список пуст."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public EmploymentResponse lookupEmployment(
            @Parameter(description = "14-значный ПИНФЛ (NNUZB).", required = true)
            @RequestParam String ni
    ) {
        return lookupService.lookupByNi(ni);
    }
}
