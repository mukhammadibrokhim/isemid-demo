package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.models.payload.responses.api.ApiResponse;
import uz.uzinfocom.isemid.services.VaccinationService;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.VACCINATION;

@RestController
@RequestMapping(VACCINATION)
@Tag(name = "Vaccination")
@RequiredArgsConstructor
public class VaccinationController {

    final VaccinationService vaccinationService;

    @GetMapping
    public ApiResponse<?> getVaccinations(
            @RequestParam String nnuzb
    ) {
        return vaccinationService.getVaccinationByNnuzbLast(nnuzb);
    }
}
