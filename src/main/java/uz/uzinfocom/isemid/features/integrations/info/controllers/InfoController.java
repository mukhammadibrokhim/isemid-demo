package uz.uzinfocom.isemid.features.integrations.info.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.integrations.info.dtos.responses.InfoIcd10Response;
import uz.uzinfocom.isemid.features.integrations.info.services.InfoService;

/**
 * Controller that provides information APIs for external systems (integrations).
 * <p>
 * This API allows external systems to retrieve information about patients
 * and other relevant data from the system.
 * </p>
 */
@RestController
@RequestMapping(ApiPaths.Info.ROOT)
@Tag(name = "Info System", description = "API для предоставления информации внешним интеграциям")
@RequiredArgsConstructor
public class InfoController {
    private final InfoService infoService;

    @Operation(
            summary = "Получить медицинскую историю пациента (форма 058) по `nnuzb`",
            description = "Этот метод возвращает список всех форм 058, связанных с пациентом, идентифицированным по NNUZB-идентификатору.")
    @GetMapping("/patient/{nnuzb}/history")
    public ApiResponse<?> getPatientMedicalHistory(@PathVariable("nnuzb") String nnuzb) {
        return infoService.getPatientMHByNnuzb(nnuzb);
    }

    @Operation(
            summary = "Получить список подтвержденных кодов МКБ-10 пациента по `nnuzb`",
            description = "Этот метод возвращает уникальные коды МКБ-10 из подтвержденных форм 058 пациента по NNUBZ.")
    @GetMapping("/patient/{nnuzb}/confirmed-mkb10")
    public ApiResponse<InfoIcd10Response> getConfirmedMkb10Codes(@PathVariable String nnuzb) {
        return infoService.getConfirmedMkb10ByNnuzb(nnuzb);
    }
}
