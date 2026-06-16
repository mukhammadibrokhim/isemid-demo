package uz.uzinfocom.app.modules.form058.application.command.cancel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form058.application.command.update.Form058UpdateMapper;
import uz.uzinfocom.app.modules.form058.application.command.update.UpdateForm058Result;
import uz.uzinfocom.app.modules.form058.application.exception.Form058NotFoundException;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ValidationException;
import uz.uzinfocom.app.modules.form058.application.shared.CurrentForm058User;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058Repository;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CancelForm058Service {

    private final Form058Repository form058Repository;
    private final Form058UpdateMapper form058UpdateMapper;
    private final CurrentForm058User currentForm058User;

    @Transactional
    public UpdateForm058Result cancel(CancelForm058Command command) {
        if (!StringUtils.hasText(command.reason())) {
            throw new Form058ValidationException("error.form058.cancel-reason-required");
        }

        Form058 form058 = form058Repository.findById(command.formId())
                .orElseThrow(() -> new Form058NotFoundException(command.formId()));
        validateSenderScope(form058);
        form058.cancel(command.reason().trim(), currentForm058User.userIdOrNull());
        return form058UpdateMapper.toResult(form058Repository.save(form058));
    }

    private void validateSenderScope(Form058 form058) {
        Long currentOrganizationId = CurrentOrganizationContext.getOptional()
                .map(Organization::getId)
                .orElseThrow(Form058ScopeViolationException::new);
        if (!Objects.equals(currentOrganizationId, form058.getSenderOrganizationId())) {
            throw new Form058ScopeViolationException();
        }
    }
}
