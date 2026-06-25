package uz.uzinfocom.app.modules.patient.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.modules.patient.domain.repository.PatientRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PatientRepositoryAdapter implements PatientRepository {

    private final JpaPatientRepository jpaPatientRepository;

    @Override
    public Patient save(Patient patient) {
        return jpaPatientRepository.save(patient);
    }

    @Override
    public Optional<Patient> findById(Long id) {
        return jpaPatientRepository.findById(id);
    }
}