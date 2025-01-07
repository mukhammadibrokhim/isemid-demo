package uz.uzinfocom.isemid.services;

import uz.uzinfocom.isemid.models.entities.Patient;

import java.util.Optional;

public interface PatientService {
    Optional<Patient> findByIdentifierTypeCodeAndValue(String name, String nnuzb);
}
