package uz.uzinfocom.isemid.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static uz.uzinfocom.isemid.utils.ApiUrlConstant.PATIENT;

@RestController
@RequestMapping(PATIENT)
@Tag(name = "Patient")
@RequiredArgsConstructor
public class PatientController {
//
//    @PostMapping
//    @ResponseStatus(CREATED)
//    public ApiResponse<Patient> addPatient(@Valid @RequestBody ReqPatient reqPatient) {
//        return new ApiResponse<>(true, "",new Patient());
//    }
}
