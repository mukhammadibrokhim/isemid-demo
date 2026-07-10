package uz.uzinfocom.app.modules.form0581.application.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581NotFoundException;
import uz.uzinfocom.app.modules.form0581.application.shared.CurrentForm0581User;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;

@Service
@RequiredArgsConstructor
public class DeleteForm0581Service {

    private final Form0581JpaRepository form0581Repository;
    private final Form0581DeleteValidator form0581DeleteValidator;
    private final CurrentForm0581User currentForm0581User;

    @Transactional
    public void delete(Long id, String reason) {
        Form0581 form0581 = form0581Repository.findActiveByIdForUpdate(id)
                .orElseThrow(() -> new Form0581NotFoundException(id));

        form0581DeleteValidator.validate(form0581);

        form0581.softDelete(currentForm0581User.userIdOrNull(), reason);
    }
}
