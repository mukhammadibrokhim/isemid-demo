package uz.uzinfocom.app.integration.dhp.immunization.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.integration.dhp.immunization.application.DhpImmunizationLookupService;
import uz.uzinfocom.app.integration.dhp.immunization.web.dto.ImmunizationResponse;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

@Tag(name = "DHP Immunization", description = "Сведения об иммунизации гражданина из DHP FHIR.")
@RestController
@RequestMapping(ApiPaths.Dhp.IMMUNIZATION)
@RequiredArgsConstructor
public class DhpImmunizationController {

    private final DhpImmunizationLookupService lookupService;

    @Operation(
            summary = "Получить сведения об иммунизации гражданина по ПИНФЛ",
            description = "Ищет ресурсы Immunization в DHP FHIR (server-to-server) по идентификатору пациента (ПИНФЛ) "
                    + "и возвращает записи о вакцинации в структуре ISEMID, новые первыми. Если сведений нет — список пуст."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ImmunizationResponse lookupImmunization(
            @Parameter(description = "14-значный ПИНФЛ (NNUZB).", required = true)
            @RequestParam String ni
    ) {
        return lookupService.lookupByNi(ni);
    }
}
