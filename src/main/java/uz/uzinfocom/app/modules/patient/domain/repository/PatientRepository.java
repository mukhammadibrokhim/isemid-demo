package uz.uzinfocom.app.modules.patient.domain.repository;

import uz.uzinfocom.app.modules.patient.domain.model.Patient;

import java.util.Optional;

public interface PatientRepository {

    Patient save(Patient patient);

    Optional<Patient> findById(Long id);
}