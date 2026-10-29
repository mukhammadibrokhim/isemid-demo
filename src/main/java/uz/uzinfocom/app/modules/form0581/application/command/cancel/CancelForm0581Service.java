package uz.uzinfocom.app.modules.form0581.application.command.cancel;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form0581.application.command.update.Form0581UpdateMapper;
import uz.uzinfocom.app.modules.form0581.application.command.update.UpdateForm0581Result;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581NotFoundException;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ValidationException;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.event.NotificationRoutingContext;
import uz.uzinfocom.app.platform.audit.event.StatusChangedEvent;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CancelForm0581Service {

    private final Form0581JpaRepository form0581Repository;
    private final Form0581UpdateMapper form0581UpdateMapper;
    private final CurrentUserProvider currentUserProvider;
    private final Form0581CancelValidator form0581CancelValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UpdateForm0581Result cancel(CancelForm0581Command command) {
        if (!StringUtils.hasText(command.reason())) {
            throw new Form0581ValidationException("error.form0581.cancel-reason-required");
        }

        Form0581 form0581 = form0581Repository.findActiveByIdForUpdate(command.formId())
                .orElseThrow(() -> new Form0581NotFoundException(command.formId()));
        form0581CancelValidator.validate(form0581);
        String oldStatus = form0581.getStatus().name();
        String reason = command.reason().trim();
        Long actorUserId = currentUserProvider.userIdOrNull();
        form0581.cancel(reason, actorUserId);
        UpdateForm0581Result result = form0581UpdateMapper.toResult(form0581Repository.save(form0581));

        eventPublisher.publishEvent(new StatusChangedEvent(
                AuditEntityType.FORM0581, form0581.getId(), oldStatus, form0581.getStatus().name(), actorUserId, reason,
                new NotificationRoutingContext.FormRouting(
                        form0581.getSenderOrganizationId(), form0581.getReceiverOrganizationId(),
                        List.of(), form0581.getSourceIntegrationClientId()
                )
        ));

        return result;
    }
}
