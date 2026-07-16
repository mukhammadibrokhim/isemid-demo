package uz.uzinfocom.app.modules.patient.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;
import uz.uzinfocom.app.modules.patient.application.mapper.PatientCommandMapper;
import uz.uzinfocom.app.modules.patient.application.validator.PatientCreateValidator;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.repository.PatientRepository;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PatientRegistrationService {

    private final PatientRepository patientRepository;
    private final PatientCommandMapper patientCommandMapper;
    private final PatientCreateValidator patientCreateValidator;

    @Transactional
    public Patient create(CreatePatientCommand command) {
        Objects.requireNonNull(command, "Patient create command must not be null");
        patientCreateValidator.validate(command);
        Patient patient = patientCommandMapper.toEntity(command);
        return patientRepository.save(patient);
    }
}
