package uz.uzinfocom.app.modules.form0581.application.command.accept;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form0581.application.command.update.Form0581UpdateMapper;
import uz.uzinfocom.app.modules.form0581.application.command.update.UpdateForm0581Result;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581NotFoundException;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.event.NotificationRoutingContext;
import uz.uzinfocom.app.platform.audit.event.StatusChangedEvent;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcceptForm0581Service {

    private final Form0581JpaRepository form0581JpaRepository;
    private final Form0581UpdateMapper form0581UpdateMapper;
    private final CurrentUserProvider currentUserProvider;
    private final Form0581AcceptValidator form0581AcceptValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UpdateForm0581Result accept(Long formId) {
        Form0581 form0581 = form0581JpaRepository.findActiveByIdForUpdate(formId)
                .orElseThrow(() -> new Form0581NotFoundException(formId));
        form0581AcceptValidator.validateAccept(form0581);
        String oldStatus = form0581.getStatus().name();
        Long actorUserId = currentUserProvider.userIdOrNull();
        form0581.accept();
        UpdateForm0581Result result = form0581UpdateMapper.toResult(form0581JpaRepository.save(form0581));

        eventPublisher.publishEvent(new StatusChangedEvent(
                AuditEntityType.FORM0581, form0581.getId(), oldStatus, form0581.getStatus().name(), actorUserId, null,
                new NotificationRoutingContext.FormRouting(
                        form0581.getSenderOrganizationId(), form0581.getReceiverOrganizationId(),
                        List.of(), form0581.getSourceIntegrationClientId()
                )
        ));

        return result;
    }
}
