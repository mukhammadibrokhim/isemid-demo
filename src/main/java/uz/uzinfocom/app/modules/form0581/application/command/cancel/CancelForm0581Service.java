package uz.uzinfocom.app.modules.form0581.application.command.cancel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form0581.application.command.update.Form0581UpdateMapper;
import uz.uzinfocom.app.modules.form0581.application.command.update.UpdateForm0581Result;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581NotFoundException;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ValidationException;
import uz.uzinfocom.app.modules.form0581.application.shared.CurrentForm0581User;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;

@Service
@RequiredArgsConstructor
public class CancelForm0581Service {

    private final Form0581JpaRepository form0581Repository;
    private final Form0581UpdateMapper form0581UpdateMapper;
    private final CurrentForm0581User currentForm0581User;
    private final Form0581CancelValidator form0581CancelValidator;

    @Transactional
    public UpdateForm0581Result cancel(CancelForm0581Command command) {
        if (!StringUtils.hasText(command.reason())) {
            throw new Form0581ValidationException("error.form0581.cancel-reason-required");
        }

        Form0581 form0581 = form0581Repository.findActiveByIdForUpdate(command.formId())
                .orElseThrow(() -> new Form0581NotFoundException(command.formId()));
        form0581CancelValidator.validate(form0581);
        form0581.cancel(command.reason().trim(), currentForm0581User.userIdOrNull());
        return form0581UpdateMapper.toResult(form0581Repository.save(form0581));
    }
}
