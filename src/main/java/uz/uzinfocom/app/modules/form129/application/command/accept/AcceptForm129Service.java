package uz.uzinfocom.app.modules.form129.application.command.accept;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form129.application.command.Form129StatusMapper;
import uz.uzinfocom.app.modules.form129.application.command.Form129StatusResult;
import uz.uzinfocom.app.modules.form129.application.exception.Form129NotFoundException;
import uz.uzinfocom.app.modules.form129.domain.model.Form129;
import uz.uzinfocom.app.modules.form129.infrastructure.persistence.repository.Form129JpaRepository;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.event.NotificationRoutingContext;
import uz.uzinfocom.app.platform.audit.event.StatusChangedEvent;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcceptForm129Service {

    private final Form129JpaRepository form129JpaRepository;
    private final Form129StatusMapper form129StatusMapper;
    private final CurrentUserProvider currentUserProvider;
    private final Form129AcceptValidator form129AcceptValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Form129StatusResult accept(AcceptForm129Command command) {
        Form129 form129 = form129JpaRepository.findByIdForUpdate(command.formId())
                .orElseThrow(() -> new Form129NotFoundException(command.formId()));
        form129AcceptValidator.validateAccept(form129);
        String oldStatus = form129.getStatus().name();
        Long actorUserId = currentUserProvider.userIdOrNull();
        form129.accept(command.receiverFullName());
        Form129StatusResult result = form129StatusMapper.toResult(form129JpaRepository.save(form129));

        eventPublisher.publishEvent(new StatusChangedEvent(
                AuditEntityType.FORM129, form129.getId(), oldStatus, form129.getStatus().name(), actorUserId, null,
                new NotificationRoutingContext.FormRouting(
                        form129.getSenderOrganizationId(), form129.getReceiverOrganizationId(),
                        List.of(), form129.getSourceIntegrationClientId()
                )
        ));

        return result;
    }
}
