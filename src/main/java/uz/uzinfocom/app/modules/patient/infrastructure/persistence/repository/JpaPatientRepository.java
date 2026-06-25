package uz.uzinfocom.app.modules.patient.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;

public interface JpaPatientRepository extends JpaRepository<Patient, Long> {
}
