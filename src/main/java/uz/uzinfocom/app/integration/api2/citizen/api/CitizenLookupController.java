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

@Tag(name = "API2 Citizen", description = "Поиск сведений о физическом лице через внешнюю систему API2.")
@RestController
@RequiredArgsConstructor
public class CitizenLookupController {

    private final CitizenLookupService citizenLookupService;
    private final MessageResolver messages;

    @Operation(
            summary = "Найти сведения о физическом лице через API2",
            description = "Выполняет поиск физического лица во внешней системе API2 по ПИНФЛ, паспортным данным или свидетельству о рождении — в зависимости от указанного типа поиска."
    )
    @GetMapping("/v1/citizen")
    public CitizenLookupResponse lookupCitizen(
            @Parameter(description = "Тип поиска: NNUZB (ПИНФЛ), PPN (паспорт) или CZ (свидетельство о рождении).", required = true)
            @RequestParam CitizenLookupType type,
            @Parameter(description = "Дата рождения — для поиска по ПИНФЛ (NNUZB) или паспорту (PPN).")
            @RequestParam(name = "birth_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate birthDate,
            @Parameter(description = "14-значный ПИНФЛ.")
            @RequestParam(required = false) String nnuzb,
            @Parameter(description = "Номер паспорта — для поиска по паспорту (PPN).")
            @RequestParam(required = false) String document,
            @Parameter(description = "Серия свидетельства о рождении — для поиска по свидетельству (CZ).")
            @RequestParam(required = false) String series,
            @Parameter(description = "Номер свидетельства о рождении — для поиска по свидетельству (CZ).")
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
