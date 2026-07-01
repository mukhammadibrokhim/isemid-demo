package uz.uzinfocom.isemid.features.integrations.vacciantion.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.integrations.vacciantion.services.VaccinationService;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping(ApiPaths.Vaccination.ROOT)
@Tag(name = "Vaccination", description = "API для получения данных о вакцинации гражданина")
@RequiredArgsConstructor
public class VaccinationController {
    private final VaccinationService vaccinationService;
    private final MessageUtil messageUtil;
    @Operation(
            summary = "Получение данных о последней вакцинации",
            description = "Возвращает информацию о последней вакцинации гражданина по уникальному идентификатору nnuzb.")
    @GetMapping
    public ApiResponse<?> getVaccinations(
            @RequestParam String nnuzb,
            @RequestHeader(name = AUTHORIZATION) String token
    ) {
        return ApiResponse.success(messageUtil, vaccinationService.getVaccinationByNnuzbLast(nnuzb, token));
    }
}
