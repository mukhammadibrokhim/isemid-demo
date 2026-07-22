package uz.uzinfocom.app.integration.api2.legalentity.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.integration.api2.legalentity.application.LegalEntityLookupService;
import uz.uzinfocom.app.integration.api2.legalentity.domain.LegalEntityLookupResult;
import uz.uzinfocom.app.platform.i18n.MessageResolver;
import uz.uzinfocom.app.shared.constants.api.ApiPaths;

@Tag(name = "API2 Legal Entity", description = "Поиск сведений о юридическом лице через внешнюю систему API2.")
@RestController
@RequestMapping(ApiPaths.LegalEntity.ROOT)
@RequiredArgsConstructor
public class LegalEntityLookupController {

    private final LegalEntityLookupService legalEntityLookupService;
    private final MessageResolver messages;

    @Operation(
            summary = "Найти сведения о юридическом лице через API2",
            description = "Выполняет поиск юридического лица во внешней системе API2 по ИНН (идентификационному номеру налогоплательщика)."
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public LegalEntityLookupResponse lookupLegalEntity(
            @Parameter(description = "9-значный идентификационный номер налогоплательщика (ИНН).", required = true)
            @RequestParam String tin
    ) {
        LegalEntityLookupResult result = legalEntityLookupService.lookupByTin(tin);

        return new LegalEntityLookupResponse(
                true,
                messages.resolve("api2.legal_entity.lookup.success"),
                result.source(),
                result.upstreamStatus(),
                result.data()
        );
    }
}
