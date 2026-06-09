package uz.uzinfocom.app.integration.api2.citizen.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.app.integration.api2.citizen.application.CitizenLookupService;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupRequest;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupResult;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;
import uz.uzinfocom.app.platform.i18n.MessageResolver;

import java.time.LocalDate;

@Tag(name = "API2 Citizen", description = "Citizen lookup through API2.")
@RestController
@RequiredArgsConstructor
public class CitizenLookupController {

    private final CitizenLookupService citizenLookupService;
    private final MessageResolver messages;

    @Operation(summary = "Lookup citizen data through API2")
    @GetMapping("/v1/citizen")
    public CitizenLookupResponse lookupCitizen(
            @Parameter(description = "Lookup type: NNUZB, PPN, or CZ.", required = true)
            @RequestParam CitizenLookupType type,
            @Parameter(description = "Birth date for NNUZB or PPN lookup.")
            @RequestParam(name = "birth_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate birthDate,
            @Parameter(description = "14 digit NNUZB.")
            @RequestParam(required = false) String nnuzb,
            @Parameter(description = "Passport document for PPN lookup.")
            @RequestParam(required = false) String document,
            @Parameter(description = "Birth certificate series for CZ lookup.")
            @RequestParam(required = false) String series,
            @Parameter(description = "Birth certificate number for CZ lookup.")
            @RequestParam(required = false) String number
    ) {
        CitizenLookupResult result = citizenLookupService.lookup(new CitizenLookupRequest(
                type,
                nnuzb,
                document,
                birthDate,
                series,
                number
        ));

        return new CitizenLookupResponse(
                true,
                messages.resolve("api2.citizen.lookup.success"),
                result.source(),
                result.upstreamStatus(),
                result.data()
        );
    }
}
