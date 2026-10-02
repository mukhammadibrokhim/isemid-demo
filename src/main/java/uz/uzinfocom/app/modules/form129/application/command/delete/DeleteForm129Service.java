package uz.uzinfocom.app.modules.form129.application.command.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.uzinfocom.app.modules.form129.application.exception.Form129NotFoundException;
import uz.uzinfocom.app.modules.form129.application.exception.Form129ValidationException;
import uz.uzinfocom.app.modules.form129.domain.model.Form129;
import uz.uzinfocom.app.modules.form129.infrastructure.persistence.repository.Form129JpaRepository;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

/**
 * Admin / super-admin-only soft delete of a Form129 — the single exception to
 * the module's "pure registry, no mutation after create" rule. The row stays;
 * {@code Form129Specification} hides it from every listing and lookup once
 * {@code deleteInfo.deleted} is set.
 */
@Service
@RequiredArgsConstructor
public class DeleteForm129Service {

    private final Form129JpaRepository form129Repository;
    private final Form129DeleteValidator form129DeleteValidator;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public void delete(Long id, String reason) {
        form129DeleteValidator.validate();

        if (!StringUtils.hasText(reason)) {
            throw new Form129ValidationException("error.form129.delete-reason-required");
        }

        Form129 form129 = form129Repository.findActiveByIdForUpdate(id)
                .orElseThrow(() -> new Form129NotFoundException(id));

        form129.softDelete(currentUserProvider.userIdOrNull(), reason.trim());
    }
}
