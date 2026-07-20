package uz.uzinfocom.app.platform.iam.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.app.platform.iam.application.shared.dto.RoleUserCountProjection;
import uz.uzinfocom.app.platform.iam.application.user.query.specification.UserScopePredicateFactory;
import uz.uzinfocom.app.platform.iam.domain.Role;
import uz.uzinfocom.app.platform.iam.domain.User;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;

import java.util.List;

/**
 * Home-dashboard user statistics — total active-user count and a breakdown
 * by role, both scoped through {@link UserScopePredicateFactory} (the same
 * collection-based "belongs to an organization within scope" EXISTS
 * predicate {@code UserSpecification} already applies to every user
 * listing/search in the platform), so a REGION-scope caller sees users
 * across their whole region, a DISTRICT-scope caller only their district,
 * etc. — never the whole platform's users regardless of caller.
 * <p>
 * Written as a plain {@code EntityManager}-backed class rather than a
 * Spring Data JPA interface because grouping by role needs a real Criteria
 * API join + group-by, and {@code UserScopePredicateFactory.applyOrganizationScope}
 * takes the Criteria {@code Root}/{@code CriteriaQuery}/{@code CriteriaBuilder}
 * directly — a derived-query or {@code @Query} JPQL string can't build that
 * predicate.
 */
@Repository
public class UserStatsRepository {

    private final EntityManager entityManager;
    private final UserScopePredicateFactory userScopePredicateFactory;

    public UserStatsRepository(EntityManager entityManager, UserScopePredicateFactory userScopePredicateFactory) {
        this.entityManager = entityManager;
        this.userScopePredicateFactory = userScopePredicateFactory;
    }

    public long countTotal(ResolvedOrganizationScope scope) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<User> root = query.from(User.class);

        Predicate scopePredicate = userScopePredicateFactory.applyOrganizationScope(root, query, cb, scope);

        query.select(cb.countDistinct(root))
                .where(cb.and(scopePredicate, cb.isTrue(root.<Boolean>get("active"))));

        return entityManager.createQuery(query).getSingleResult();
    }

    public List<RoleUserCountProjection> countByRole(ResolvedOrganizationScope scope) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<User> root = query.from(User.class);
        Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);

        Predicate scopePredicate = userScopePredicateFactory.applyOrganizationScope(root, query, cb, scope);

        query.multiselect(roleJoin.<String>get("name"), cb.countDistinct(root))
                .where(cb.and(scopePredicate, cb.isTrue(root.<Boolean>get("active"))))
                .groupBy(roleJoin.get("name"));

        return entityManager.createQuery(query).getResultList().stream()
                .map(tuple -> new RoleUserCountProjection(tuple.get(0, String.class), tuple.get(1, Long.class)))
                .toList();
    }
}
