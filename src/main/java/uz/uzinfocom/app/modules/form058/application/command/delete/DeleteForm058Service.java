package uz.uzinfocom.app.features.form058.application.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form058.application.command.delete.Form058DeleteValidator;
import uz.uzinfocom.app.modules.form058.application.exception.Form058NotFoundException;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058Repository;

@Service
@RequiredArgsConstructor
public class DeleteForm058Service {

    private final Form058Repository form058Repository;
    private final Form058DeleteValidator form058DeleteValidator;

    @Transactional
    public void delete(Long formId) {
        Form058 form058 = form058Repository.findById(formId)
                .orElseThrow(() -> new Form058NotFoundException(formId));
        boolean linkedCardsExist = form058Repository.existsByIdAndHasLinkedCardsTrue(formId);
        form058DeleteValidator.validate(form058, linkedCardsExist);
        form058Repository.delete(form058);
    }
}
