package uz.uzinfocom.app.modules.form129.infrastructure.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.app.modules.form129.application.query.projection.Form129TableProjection;
import uz.uzinfocom.app.modules.form129.domain.model.Form129;

public interface Form129TableRepository {

    /**
     * One page of table rows, selected as plain columns. Loading entities here would
     * cost an extra organization query per distinct creator/updater organization:
     * {@code createdOrg}/{@code updatedOrg} join on a non-primary-key column, so
     * Hibernate fetches them eagerly despite {@code LAZY}.
     */
    Slice<Form129TableProjection> findTableSlice(Specification<Form129> specification, Pageable pageable);
}
