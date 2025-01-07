package uz.uzinfocom.isemid.services.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.models.entities.Patient;
import uz.uzinfocom.isemid.repositories.PatientRepository;
import uz.uzinfocom.isemid.services.PatientService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    final PatientRepository patientRepository;

    @Override
    public Optional<Patient> findByIdentifierTypeCodeAndValue(String typeCode, String value) {
        return patientRepository.findByIdentifierTypeAndValue(typeCode, value);
    }
}
