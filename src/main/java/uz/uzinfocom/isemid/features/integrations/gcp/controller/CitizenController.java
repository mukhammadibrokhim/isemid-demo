package uz.uzinfocom.isemid.features.integrations.gcp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiException;
import uz.uzinfocom.isemid.features.integrations.gcp.service.CitizenService;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping(ApiPaths.Citizen.ROOT)
@Tag(name = "Citizen", description = "Получение данных о гражданине из GCP, если гражданин не найден в базе данных")
@RequiredArgsConstructor
public class CitizenController {

   private final CitizenService citizenService;

    @Operation(
            summary = "Получение информации о гражданине",
            description = """
                Возвращает данные гражданина на основе предоставленных параметров:
                - При указании `nnuzb` и `photo` — поиск по уникальному идентификатору и фотографии.
                - При указании `series` и `number` — поиск данных по документу ребенка.
                Если параметры некорректны или отсутствуют — возвращается ошибка.
                """,
            parameters = {
                    @Parameter(name = "nnuzb", description = "Уникальный ID гражданина (nnuzb)"),
                    @Parameter(name = "photo", description = "Фотография гражданина"),
                    @Parameter(name = "series", description = "Серия документа ребенка"),
                    @Parameter(name = "number", description = "Номер документа ребенка")})
    @GetMapping
    public ResponseEntity<Object> getCitizen(
            @RequestParam(required = false) String nnuzb,
            @RequestParam(required = false) String photo,
            @RequestParam(required = false) String series,
            @RequestParam(required = false) String number,
            @Parameter(hidden = true) @RequestHeader(AUTHORIZATION) String token

    ) {
        if (nnuzb != null && photo != null) {
            return ResponseEntity.ok(citizenService.getCitizenByNnuzb(nnuzb, photo, token));
        } else if (series != null && number != null) {
            return ResponseEntity.ok(citizenService.getCitizenByChild(series, number, token));
        } else throw new ApiException(BAD_REQUEST, "Неверные параметры запроса");
    }

    @Operation(
            summary = "Генерация данных гражданина по nnuzb",
            description = "Возвращает информацию о гражданине по уникальному идентификатору nnuzb с использованием токена."
    )
    @GetMapping("/qr")
    public ResponseEntity<?> genCitizenByNnuzb(@RequestParam(required = false) String nnuzb) {
        return citizenService.getCitizenByNnuzbByToken(nnuzb);
    }
}
