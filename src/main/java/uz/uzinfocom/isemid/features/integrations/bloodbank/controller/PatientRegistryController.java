package uz.uzinfocom.isemid.features.integrations.bloodbank.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.uzinfocom.isemid.commons.constants.ApiPaths;
import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.commons.utils.MessageUtil;
import uz.uzinfocom.isemid.features.integrations.bloodbank.dtos.InfectedPatientResponse;
import uz.uzinfocom.isemid.features.integrations.bloodbank.service.PatientRegistryService;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.PatientRegistry.ROOT)
@Tag(name = "Patient registry", description = "API для провеки донора на инфекционные заболевания")
@RequiredArgsConstructor
public class PatientRegistryController {

    private final MessageUtil messageUtil;
    private final PatientRegistryService patientRegistryService;

    @Operation(summary = "Для системы крови проверка доноров", description = "Проверяет донора на наличия инфекционных заболеваний.")
    @GetMapping("/has-confirmed-infection")
    public ApiResponse<List<InfectedPatientResponse>> getInfectedPatient(@RequestParam String nnuzb) {
        return ApiResponse.success(messageUtil, patientRegistryService.getInfectedPatientResponses(nnuzb));
    }

}
