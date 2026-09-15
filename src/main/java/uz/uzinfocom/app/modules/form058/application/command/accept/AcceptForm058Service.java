package uz.uzinfocom.app.modules.form058.application.command.accept;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form058.application.command.update.Form058UpdateMapper;
import uz.uzinfocom.app.modules.form058.application.command.update.UpdateForm058Result;
import uz.uzinfocom.app.modules.form058.application.exception.Form058NotFoundException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.event.NotificationRoutingContext;
import uz.uzinfocom.app.platform.audit.event.StatusChangedEvent;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcceptForm058Service {

    private final Form058JpaRepository form058JpaRepository;
    private final Form058UpdateMapper form058UpdateMapper;
    private final CurrentUserProvider currentUserProvider;
    private final Form058AcceptValidator form058AcceptValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UpdateForm058Result accept(Long formId) {
        Form058 form058 = findRequired(formId);
        form058AcceptValidator.validateAccept(form058);
        String oldStatus = form058.getStatus().name();
        Long actorUserId = currentUserProvider.userIdOrNull();
        form058.accept();
        UpdateForm058Result result = form058UpdateMapper.toResult(form058JpaRepository.save(form058));

        eventPublisher.publishEvent(new StatusChangedEvent(
                AuditEntityType.FORM058, form058.getId(), oldStatus, form058.getStatus().name(), actorUserId, null,
                new NotificationRoutingContext.FormRouting(
                        form058.getSenderOrganizationId(), form058.getReceiverOrganizationId(),
                        List.of(), form058.getSourceIntegrationClientId()
                )
        ));

        return result;
    }

    private Form058 findRequired(Long id) {
        return form058JpaRepository.findActiveByIdForUpdate(id)
                .orElseThrow(() -> new Form058NotFoundException(id));
    }
}
