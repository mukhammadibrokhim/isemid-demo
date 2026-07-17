package uz.uzinfocom.app.modules.form058.application.command.cancel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form058.application.command.update.Form058UpdateMapper;
import uz.uzinfocom.app.modules.form058.application.command.update.UpdateForm058Result;
import uz.uzinfocom.app.modules.form058.application.exception.Form058NotFoundException;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ValidationException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

@Service
@RequiredArgsConstructor
public class CancelForm058Service {

    private final Form058JpaRepository form058Repository;
    private final Form058UpdateMapper form058UpdateMapper;
    private final CurrentUserProvider currentUserProvider;
    private final Form058CancelValidator form058CancelValidator;

    @Transactional
    public UpdateForm058Result cancel(CancelForm058Command command) {
        if (!StringUtils.hasText(command.reason())) {
            throw new Form058ValidationException("error.form058.cancel-reason-required");
        }

        Form058 form058 = form058Repository.findActiveByIdForUpdate(command.formId())
                .orElseThrow(() -> new Form058NotFoundException(command.formId()));
        form058CancelValidator.validate(form058);
        form058.cancel(command.reason().trim(), currentUserProvider.userIdOrNull());
        return form058UpdateMapper.toResult(form058Repository.save(form058));
    }
}
