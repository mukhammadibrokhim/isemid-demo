package uz.uzinfocom.app.modules.form058.application.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form058.application.exception.Form058NotFoundException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

@Service
@RequiredArgsConstructor
public class DeleteForm058Service {

    private final Form058JpaRepository form058Repository;
    private final Form058DeleteValidator form058DeleteValidator;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public void delete(Long id, String reason) {
        Form058 form058 = form058Repository.findActiveByIdForUpdate(id)
                .orElseThrow(() -> new Form058NotFoundException(id));

        form058DeleteValidator.validate(form058);

        form058.softDelete(currentUserProvider.userIdOrNull(), reason);
    }
}