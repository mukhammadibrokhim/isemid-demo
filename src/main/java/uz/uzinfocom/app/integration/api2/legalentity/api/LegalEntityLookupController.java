package uz.uzinfocom.app.integration.api2.legalentity.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.integration.api2.legalentity.application.LegalEntityLookupService;
import uz.uzinfocom.app.integration.api2.legalentity.domain.LegalEntityLookupResult;

@Tag(name = "API2 Legal Entity", description = "Legal entity lookup through API2.")
@RestController
@RequiredArgsConstructor
public class LegalEntityLookupController {

    private final LegalEntityLookupService legalEntityLookupService;

    @Operation(summary = "Lookup legal entity data through API2")
    @GetMapping("/v1/legal-entity")
    public LegalEntityLookupResponse lookupLegalEntity(
            @Parameter(description = "9 digit taxpayer identification number.", required = true)
            @RequestParam String tin
    ) {
        LegalEntityLookupResult result = legalEntityLookupService.lookupByTin(tin);

        return new LegalEntityLookupResponse(
                true,
                "Legal entity lookup completed.",
                result.source(),
                result.upstreamStatus(),
                result.data()
        );
    }
}
