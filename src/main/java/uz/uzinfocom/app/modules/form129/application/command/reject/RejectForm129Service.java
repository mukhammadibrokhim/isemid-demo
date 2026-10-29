package uz.uzinfocom.app.modules.form129.application.command.reject;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form129.application.command.Form129StatusMapper;
import uz.uzinfocom.app.modules.form129.application.command.Form129StatusResult;
import uz.uzinfocom.app.modules.form129.application.exception.Form129NotFoundException;
import uz.uzinfocom.app.modules.form129.application.exception.Form129ValidationException;
import uz.uzinfocom.app.modules.form129.domain.model.Form129;
import uz.uzinfocom.app.modules.form129.infrastructure.persistence.repository.Form129JpaRepository;
import uz.uzinfocom.app.platform.audit.domain.AuditEntityType;
import uz.uzinfocom.app.platform.audit.event.NotificationRoutingContext;
import uz.uzinfocom.app.platform.audit.event.StatusChangedEvent;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RejectForm129Service {

    private final Form129JpaRepository form129Repository;
    private final Form129StatusMapper form129StatusMapper;
    private final CurrentUserProvider currentUserProvider;
    private final Form129RejectValidator form129RejectValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Form129StatusResult reject(RejectForm129Command command) {
        if (!StringUtils.hasText(command.reason())) {
            throw new Form129ValidationException("error.form129.reject-reason-required");
        }

        Form129 form129 = form129Repository.findByIdForUpdate(command.formId())
                .orElseThrow(() -> new Form129NotFoundException(command.formId()));
        form129RejectValidator.validate(form129);
        String oldStatus = form129.getStatus().name();
        String reason = command.reason().trim();
        Long actorUserId = currentUserProvider.userIdOrNull();
        form129.reject(reason, actorUserId);
        Form129StatusResult result = form129StatusMapper.toResult(form129Repository.save(form129));

        eventPublisher.publishEvent(new StatusChangedEvent(
                AuditEntityType.FORM129, form129.getId(), oldStatus, form129.getStatus().name(), actorUserId, reason,
                new NotificationRoutingContext.FormRouting(
                        form129.getSenderOrganizationId(), form129.getReceiverOrganizationId(),
                        List.of(), form129.getSourceIntegrationClientId()
                )
        ));

        return result;
    }
}
