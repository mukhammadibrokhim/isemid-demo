package uz.uzinfocom.app.modules.form0581.application.command.approve;

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
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApproveForm0581Service {

    private final Form0581JpaRepository form0581JpaRepository;
    private final Form0581UpdateMapper form0581UpdateMapper;
    private final CurrentUserProvider currentUserProvider;
    private final Form0581ApprovalValidator form0581ApprovalValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UpdateForm0581Result approve(ApproveForm0581Command command) {
        return approveLocked(findRequired(command.formId()), command.finalIcd10Code(), command.finalIcd10Name());
    }

    /**
     * Approval for the inbound-integration surface, where the form is addressed by id or uuid.
     * Re-sending the same final diagnosis for an already approved form is a no-op that returns
     * the current state, so a retry after a lost response does not fail.
     */
    @Transactional
    public UpdateForm0581Result confirm(Long formId, UUID formUuid, String finalIcd10Code, String finalIcd10Name) {
        Form0581 form0581 = formId != null
                ? findRequired(formId)
                : form0581JpaRepository.findActiveByUuidForUpdate(formUuid)
                        .orElseThrow(() -> new Form0581NotFoundException(formUuid));

        if (form0581.isApproved()
                && form0581.getDiagnosisInfo() != null
                && Objects.equals(form0581.getDiagnosisInfo().getFinalIcd10Code(), finalIcd10Code.trim())) {
            form0581ApprovalValidator.validateOwnership(form0581);
            return form0581UpdateMapper.toResult(form0581);
        }
        return approveLocked(form0581, finalIcd10Code, finalIcd10Name);
    }

    private UpdateForm0581Result approveLocked(Form0581 form0581, String finalIcd10Code, String finalIcd10Name) {
        if (!StringUtils.hasText(finalIcd10Code) || !StringUtils.hasText(finalIcd10Name)) {
            throw new Form0581ValidationException("error.form0581.approval-not-allowed");
        }

        form0581ApprovalValidator.validateApprove(form0581);
        String oldStatus = form0581.getStatus().name();
        Long actorUserId = currentUserProvider.userIdOrNull();
        form0581.approve(
                finalIcd10Code.trim(),
                finalIcd10Name.trim(),
                actorUserId,
                form0581.getSenderOrganizationId()
        );
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

    private Form0581 findRequired(Long id) {
        return form0581JpaRepository.findActiveByIdForUpdate(id)
                .orElseThrow(() -> new Form0581NotFoundException(id));
    }
}
