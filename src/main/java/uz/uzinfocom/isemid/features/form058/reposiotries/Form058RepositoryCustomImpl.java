package uz.uzinfocom.isemid.features.form058.reposiotries;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.form058.models.Form058;

import java.util.stream.Stream;

@Repository
public class Form058RepositoryCustomImpl implements Form058RepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Stream<Form058> streamAll(Specification<Form058> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Form058> cq = cb.createQuery(Form058.class);
        Root<Form058> root = cq.from(Form058.class);

        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, cq, cb);
            if (predicate != null) {
                cq.where(predicate);
            }
        }

        cq.orderBy(cb.asc(root.get("id")));  // Assuming id exists and is a good ordering field

        TypedQuery<Form058> typedQuery = entityManager.createQuery(cq);
        Stream<Form058> stream = typedQuery
                .unwrap(org.hibernate.query.Query.class)
                .setFetchSize(1000)
                .setReadOnly(true)
                .stream();

        return stream;
    }
}