package uz.uzinfocom.app.modules.form0581.application.command.approve;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form0581.application.command.update.Form0581UpdateMapper;
import uz.uzinfocom.app.modules.form0581.application.command.update.UpdateForm0581Result;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581NotFoundException;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ValidationException;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

@Service
@RequiredArgsConstructor
public class ApproveForm0581Service {

    private final Form0581JpaRepository form0581JpaRepository;
    private final Form0581UpdateMapper form0581UpdateMapper;
    private final CurrentUserProvider currentUserProvider;
    private final Form0581ApprovalValidator form0581ApprovalValidator;

    @Transactional
    public UpdateForm0581Result approve(ApproveForm0581Command command) {
        if (!StringUtils.hasText(command.finalMkb10Code()) || !StringUtils.hasText(command.finalMkb10Name())) {
            throw new Form0581ValidationException("error.form0581.approval-not-allowed");
        }

        Form0581 form0581 = findRequired(command.formId());
        form0581ApprovalValidator.validateApprove(form0581);
        form0581.approve(
                command.finalMkb10Code().trim(),
                command.finalMkb10Name().trim(),
                currentUserProvider.userIdOrNull(),
                form0581.getReceiverOrganizationId()
        );
        return form0581UpdateMapper.toResult(form0581JpaRepository.save(form0581));
    }

    @Transactional
    public UpdateForm0581Result notApprove(NotApproveForm0581Command command) {
        Form0581 form0581 = findRequired(command.formId());
        form0581ApprovalValidator.validateNotApprove(form0581);
        form0581.notApprove(StringUtils.hasText(command.reason()) ? command.reason().trim() : null);
        return form0581UpdateMapper.toResult(form0581JpaRepository.save(form0581));
    }

    private Form0581 findRequired(Long id) {
        return form0581JpaRepository.findActiveByIdForUpdate(id)
                .orElseThrow(() -> new Form0581NotFoundException(id));
    }
}
