package uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form058.application.query.Form058Filter;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.projection.Form058StatusCountProjection;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.projection.Form058TableProjection;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.specification.Form058Specification;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class Form058QueryRepository {

    private final Form058Repository form058Repository;

    @Transactional(readOnly = true)
    public Page<Form058TableProjection> findTable(
            Form058Filter filter,
            Long organizationId,
            boolean received,
            Map<String, String> allowedSortFields
    ) {
        return form058Repository.findBy(
                Form058Specification.table(filter, organizationId, received),
                query -> query
                        .as(Form058TableProjection.class)
                        .page(PageableUtils.of(filter, allowedSortFields))
        );
    }

    public Optional<Form058> findVisibleById(Long id, Long organizationId) {
        return form058Repository.findOne(Form058Specification.visibleById(id, organizationId));
    }

    public Optional<Form058> findLatestVisibleByNnuzb(String nnuzb, Long organizationId) {
        return form058Repository
                .findAll(
                        Form058Specification.visibleByNnuzb(nnuzb, organizationId),
                        PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id"))
                )
                .stream()
                .findFirst();
    }

    public Optional<Form058> findVisibleByCard(Long cardId, Long organizationId) {
        return form058Repository.findOne(Form058Specification.visibleByCard(cardId, organizationId));
    }

    public List<Form058StatusCountProjection> countStatusesByOrganization(Long organizationId) {
        return form058Repository.countStatusesByOrganization(organizationId);
    }
}
