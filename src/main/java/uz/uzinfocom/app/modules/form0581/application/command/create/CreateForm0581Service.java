package uz.uzinfocom.app.modules.form0581.application.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form0581.application.command.Form0581OtherInjuredPersonMapper;
import uz.uzinfocom.app.modules.form0581.application.validator.Form0581CreateValidator;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581OtherInjuredPerson;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.modules.patient.application.service.PatientRegistrationService;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.platform.persistence.sync.ChildCollectionSync;

@Service
@RequiredArgsConstructor
public class CreateForm0581Service {

    private final Form0581JpaRepository form0581Repository;
    private final Form0581CreateMapper form0581CreateMapper;
    private final Form0581CreateValidator form0581CreateValidator;
    private final Form0581OtherInjuredPersonMapper otherInjuredPersonMapper;
    private final PatientRegistrationService patientRegistrationService;

    @Transactional
    public CreateForm0581Result create(CreateForm0581Command command) {
        form0581CreateValidator.validate(command);
        Patient patient = patientRegistrationService.create(command.patient());
        Form0581 form0581 = form0581CreateMapper.toEntity(command);
        form0581.setPatient(patient);

        ChildCollectionSync.sync(
                form0581,
                form0581.getOtherInjuredPeople(),
                command.otherInjuredPeople(),
                otherInjuredPersonMapper::toEntity,
                otherInjuredPersonMapper::update,
                Form0581OtherInjuredPerson::setForm0581
        );

        return form0581CreateMapper.toResult(form0581Repository.save(form0581));
    }
}
