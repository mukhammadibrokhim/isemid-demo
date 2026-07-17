package uz.uzinfocom.app.modules.form058.application.command.approve;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form058.application.command.update.Form058UpdateMapper;
import uz.uzinfocom.app.modules.form058.application.command.update.UpdateForm058Result;
import uz.uzinfocom.app.modules.form058.application.exception.Form058NotFoundException;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ValidationException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ApproveForm058Service {

    private final Form058JpaRepository form058JpaRepository;
    private final Form058UpdateMapper form058UpdateMapper;
    private final CurrentUserProvider currentUserProvider;
    private final Form058ApprovalValidator form058ApprovalValidator;

    @Transactional
    public UpdateForm058Result approve(ApproveForm058Command command) {
        if (!StringUtils.hasText(command.finalMkb10Code()) || !StringUtils.hasText(command.finalMkb10Name())) {
            throw new Form058ValidationException("error.form058.approval-not-allowed");
        }

        Form058 form058 = findRequired(command.formId());
        form058ApprovalValidator.validateApprove(form058);
        form058.approve(
                command.finalMkb10Code().trim(),
                command.finalMkb10Name().trim(),
                currentUserProvider.userIdOrNull(),
                form058.getReceiverOrganizationId()
        );
        return form058UpdateMapper.toResult(form058JpaRepository.save(form058));
    }

    @Transactional
    public UpdateForm058Result notApprove(NotApproveForm058Command command) {
        Form058 form058 = findRequired(command.formId());
        form058ApprovalValidator.validateNotApprove(form058);
        form058.notApprove(StringUtils.hasText(command.reason()) ? command.reason().trim() : null);
        return form058UpdateMapper.toResult(form058JpaRepository.save(form058));
    }

    @Transactional
    public UpdateForm058Result approveDiagnosis(ApproveForm058Command command) {
        Form058 form058 = findRequired(command.formId());
        validateReceiverScope(form058);
        form058.updateFinalDiagnosis(command.finalMkb10Code(), command.finalMkb10Name());
        return form058UpdateMapper.toResult(form058JpaRepository.save(form058));
    }

    private Form058 findRequired(Long id) {
        return form058JpaRepository.findActiveByIdForUpdate(id)
                .orElseThrow(() -> new Form058NotFoundException(id));
    }

    private Long validateReceiverScope(Form058 form058) {
        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form058ScopeViolationException::new);
        if (!Objects.equals(currentOrganizationId, form058.getReceiverOrganizationId())) {
            throw new Form058ScopeViolationException();
        }
        return currentOrganizationId;
    }
}
