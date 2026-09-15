package uz.uzinfocom.app.modules.form129.application.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form129.application.validator.Form129CreateValidator;
import uz.uzinfocom.app.modules.form129.domain.model.Form129;
import uz.uzinfocom.app.modules.form129.infrastructure.persistence.repository.Form129JpaRepository;
import uz.uzinfocom.app.modules.patient.application.service.PatientRegistrationService;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.event.EntityCreatedEvent;
import uz.uzinfocom.app.platform.audit.event.NotificationRoutingContext;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateForm129Service {

    private final Form129JpaRepository form129Repository;
    private final Form129CreateMapper form129CreateMapper;
    private final Form129CreateValidator form129CreateValidator;
    private final PatientRegistrationService patientRegistrationService;
    private final CurrentUserProvider currentUserProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreateForm129Result create(CreateForm129Command command) {
        form129CreateValidator.validate(command);
        Patient patient = patientRegistrationService.create(command.patient());
        Form129 form129 = form129CreateMapper.toEntity(command);
        form129.setPatient(patient);

        Form129 saved = form129Repository.save(form129);

        eventPublisher.publishEvent(new EntityCreatedEvent(
                AuditEntityType.FORM129, saved.getId(), currentUserProvider.userIdOrNull(),
                new NotificationRoutingContext.FormRouting(
                        saved.getSenderOrganizationId(), saved.getReceiverOrganizationId(),
                        List.of(), saved.getSourceIntegrationClientId()
                )
        ));

        return form129CreateMapper.toResult(saved);
    }
}
