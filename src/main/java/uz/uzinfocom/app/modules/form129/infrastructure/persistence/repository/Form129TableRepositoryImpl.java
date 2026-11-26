package uz.uzinfocom.app.modules.form129.infrastructure.persistence.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import uz.uzinfocom.app.modules.form129.application.query.projection.Form129TableProjection;
import uz.uzinfocom.app.modules.form129.domain.model.Form129;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;

import java.util.List;

@RequiredArgsConstructor
public class Form129TableRepositoryImpl implements Form129TableRepository {

    private final EntityManager entityManager;

    @Override
    public Slice<Form129TableProjection> findTableSlice(Specification<Form129> specification, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Form129TableProjection> query = cb.createQuery(Form129TableProjection.class);
        Root<Form129> root = query.from(Form129.class);
        Join<Form129, Patient> patient = root.join("patient");

        query.select(cb.construct(
                Form129TableProjection.class,
                root.get("id"),
                root.get("uuid"),
                root.get("createdAt"),
                root.get("status"),
                root.get("source"),
                root.get("senderOrganizationId"),
                root.get("receiverOrganizationId"),
                patient.get("id"),
                patient.get("firstName"),
                patient.get("lastName"),
                patient.get("middleName")
        ));

        Predicate predicate = specification.toPredicate(root, query, cb);
        if (predicate != null) {
            query.where(predicate);
        }

        query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, cb));

        // One row past the page tells whether a next page exists.
        List<Form129TableProjection> rows = entityManager.createQuery(query)
                .setFirstResult(Math.toIntExact(pageable.getOffset()))
                .setMaxResults(pageable.getPageSize() + 1)
                .getResultList();

        boolean hasNext = rows.size() > pageable.getPageSize();

        return new SliceImpl<>(
                hasNext ? rows.subList(0, pageable.getPageSize()) : rows,
                pageable,
                hasNext
        );
    }
}
