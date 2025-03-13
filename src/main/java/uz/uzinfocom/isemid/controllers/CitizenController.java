package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.exceptioins.ApiException;
import uz.uzinfocom.isemid.services.CitizenService;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uz.uzinfocom.isemid.utils.ApiUrlConstant.CITIZEN;

@RestController
@RequestMapping(CITIZEN)
@Tag(name = "Citizen", description = "Получение данных о гражданине из GCP, если гражданин не найден в базе данных")
@RequiredArgsConstructor
public class CitizenController {

    final CitizenService citizenService;

    @Operation(
            summary = "Получение информации о гражданине",
            description = """
                        Получает информацию о гражданине на основе предоставленных параметров:
                        - Если указаны `nnuzb` и `photo`, то информация о гражданине будет получена по уникальному идентификатору nnuzb и фотографии.
                        - Если указаны `series` и `number`, то информация будет получена на основе данных о ребенке.
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

    @GetMapping("/qr")
    public ResponseEntity<?> genCitizenByNnuzb(@RequestParam(required = false) String nnuzb) {
        return citizenService.getCitizenByNnuzbByToken(nnuzb);
    }
}
