package uz.uzinfocom.app.modules.form058.application.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form058.application.exception.Form058NotFoundException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;
import uz.uzinfocom.app.modules.patient.application.service.PatientIdentifierSync;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;

@Service
@RequiredArgsConstructor
public class UpdateForm058Service {

    private final Form058JpaRepository form058Repository;
    private final Form058UpdateMapper form058UpdateMapper;
    private final Form058UpdateValidator form058UpdateValidator;

    @Transactional
    public UpdateForm058Result update(UpdateForm058Command command) {
        Form058 form058 = findRequired(command.id());
        form058UpdateValidator.validate(form058, command);
        form058UpdateMapper.update(command, form058);
        updatePatient(command, form058.getPatient());
        return form058UpdateMapper.toResult(form058Repository.save(form058));
    }

    private Form058 findRequired(Long id) {
        return form058Repository.findActiveByIdForUpdate(id)
                .orElseThrow(() -> new Form058NotFoundException(id));
    }

    private void updatePatient(UpdateForm058Command command, Patient patient) {
        CreatePatientCommand patientCommand = command.patient();
        if (patient == null || patientCommand == null) {
            return;
        }

        if (patientCommand.firstName() != null) {
            patient.setFirstName(patientCommand.firstName());
        }
        if (patientCommand.lastName() != null) {
            patient.setLastName(patientCommand.lastName());
        }
        if (patientCommand.middleName() != null) {
            patient.setMiddleName(patientCommand.middleName());
        }
        if (patientCommand.birthDate() != null) {
            patient.setBirthDate(patientCommand.birthDate());
        }
        if (patientCommand.genderCode() != null) {
            patient.setGenderCode(patientCommand.genderCode());
        }
        if (patientCommand.phoneNumber() != null) {
            patient.setPhoneNumber(patientCommand.phoneNumber());
        }
        patientCommand.identifiers().forEach(identifier -> PatientIdentifierSync.upsert(patient, identifier));
    }
}
