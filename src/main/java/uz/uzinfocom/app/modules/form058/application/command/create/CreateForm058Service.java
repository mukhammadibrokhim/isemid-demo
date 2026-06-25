package uz.uzinfocom.app.modules.form058.application.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form058.application.validator.Form058CreateValidator;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.modules.patient.application.service.PatientRegistrationService;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;

@Service
@RequiredArgsConstructor
public class CreateForm058Service {

    private final Form058JpaRepository form058Repository;
    private final Form058CreateMapper form058CreateMapper;
    private final Form058CreateValidator form058CreateValidator;
    private final PatientRegistrationService patientRegistrationService;

    @Transactional
    public CreateForm058Result create(CreateForm058Command command) {
        form058CreateValidator.validate(command);
        Patient patient = patientRegistrationService.create(command.patient());
        Form058 form058 = form058CreateMapper.toEntity(command);
        form058.setPatient(patient);
        return form058CreateMapper.toResult(form058Repository.save(form058));
    }
}
