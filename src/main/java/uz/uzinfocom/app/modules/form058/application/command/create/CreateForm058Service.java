package uz.uzinfocom.app.modules.form058.application.command.create;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form058.application.shared.Form058AffiliatedOrganizationsResolver;
import uz.uzinfocom.app.modules.form058.application.validator.Form058CreateValidator;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.modules.patient.application.service.PatientRegistrationService;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.event.EntityCreatedEvent;
import uz.uzinfocom.app.platform.audit.event.NotificationRoutingContext;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CreateForm058Service {

    private final Form058JpaRepository form058Repository;
    private final Form058CreateMapper form058CreateMapper;
    private final Form058CreateValidator form058CreateValidator;
    private final PatientRegistrationService patientRegistrationService;
    private final CurrentUserProvider currentUserProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreateForm058Result create(CreateForm058Command command) {
        form058CreateValidator.validate(command);
        Patient patient = patientRegistrationService.create(command.patient());
        Form058 form058 = form058CreateMapper.toEntity(command);
        form058.setPatient(patient);
        Form058 saved = form058Repository.save(form058);

        Set<Long> affiliatedOrganizationIds = Form058AffiliatedOrganizationsResolver.resolve(saved.getPatient());
        affiliatedOrganizationIds.remove(saved.getSenderOrganizationId());
        affiliatedOrganizationIds.remove(saved.getReceiverOrganizationId());

        eventPublisher.publishEvent(new EntityCreatedEvent(
                AuditEntityType.FORM058, saved.getId(), currentUserProvider.userIdOrNull(),
                new NotificationRoutingContext.FormRouting(
                        saved.getSenderOrganizationId(), saved.getReceiverOrganizationId(),
                        List.copyOf(affiliatedOrganizationIds), null
                )
        ));

        return form058CreateMapper.toResult(saved);
    }
}
