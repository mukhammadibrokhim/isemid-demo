package uz.uzinfocom.app.platform.stats.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Template-method base for Criteria API "group by one dimension, count rows"
 * aggregation queries — the shape every stats repository in this codebase
 * needs (status breakdown, top-N diagnosis codes, day/month-bucketed counts,
 * cross-organization breakdowns). A subclass supplies only the entity type
 * and, per query, the dimension expression, an optional extra filter, and
 * the response mapper; this class owns the repeated Criteria API plumbing
 * (tuple selection, grouping, ordering, result mapping) so it is written
 * exactly once instead of once per entity type.
 *
 * @param <T> the aggregated entity type (e.g. {@code Form058}, {@code Card})
 */
public abstract class AbstractCaseStatsRepository<T> {

    protected static final ZoneId APPLICATION_ZONE = ZoneId.of("Asia/Tashkent");

    protected final EntityManager entityManager;
    private final Class<T> entityClass;

    protected AbstractCaseStatsRepository(EntityManager entityManager, Class<T> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
    }

    /**
     * Every entity aggregated here is expected to be soft-deletable via a
     * {@code deleteInfo.deleted} embeddable. Entities without one (e.g. Card,
     * Act) override this to {@code cb.conjunction()} (always true).
     */
    protected Predicate notDeleted(Root<T> root, CriteriaBuilder cb) {
        return cb.isFalse(root.get("deleteInfo").get("deleted"));
    }

    /**
     * Group-by-dimension row count, with an optional caller-supplied filter
     * (organization scope, an id restriction, or {@code null} for none).
     */
    protected <D, R> List<R> countGrouped(
            BiFunction<Root<T>, CriteriaBuilder, Expression<D>> dimensionFn,
            BiFunction<Root<T>, CriteriaBuilder, Predicate> filterFn,
            BiFunction<D, Long, R> mapper
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<T> root = query.from(entityClass);

        Expression<D> dimension = dimensionFn.apply(root, cb);

        query.select(cb.tuple(dimension, cb.count(root)))
                .where(combinedPredicate(root, cb, filterFn))
                .groupBy(dimension);

        return runAndMap(query, null, mapper);
    }

    /**
     * Same shape as {@link #countGrouped}, but ordered by count descending
     * and capped at {@code limit} — for "top N" queries. The dimension is
     * additionally required to be non-null (mirrors excluding blank/absent
     * values from a top-N ranking).
     */
    protected <D, R> List<R> topGrouped(
            BiFunction<Root<T>, CriteriaBuilder, Expression<D>> dimensionFn,
            BiFunction<Root<T>, CriteriaBuilder, Predicate> filterFn,
            int limit,
            BiFunction<D, Long, R> mapper
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<T> root = query.from(entityClass);

        Expression<D> dimension = dimensionFn.apply(root, cb);

        query.select(cb.tuple(dimension, cb.count(root)))
                .where(combinedPredicate(root, cb, filterFn, cb.isNotNull(dimension)))
                .groupBy(dimension)
                .orderBy(cb.desc(cb.count(root)));

        return runAndMap(query, limit, mapper);
    }

    /**
     * Bare {@code COUNT(*)} with an optional caller-supplied filter — no
     * grouping. For totals that need to be filtered further than just
     * "grouped by X" (e.g. "everything except one status"), so a caller
     * never has to fetch a grouped breakdown and sum/subtract it in Java
     * just to get a single number the database can already produce
     * directly.
     */
    protected long countAll(BiFunction<Root<T>, CriteriaBuilder, Predicate> filterFn) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> root = query.from(entityClass);

        query.select(cb.count(root)).where(combinedPredicate(root, cb, filterFn));

        return entityManager.createQuery(query).getSingleResult();
    }

    /**
     * Count grouped by a {@code date_trunc(unit, createdAt)} bucket
     * ("day", "month", ...), ordered by the bucket ascending, with an
     * optional inclusive {@code [fromDate, toDate]} range and the same
     * optional caller-supplied filter as {@link #countGrouped}.
     */
    protected <R> List<R> countByDateBucket(
            String unit,
            BiFunction<Root<T>, CriteriaBuilder, Predicate> filterFn,
            LocalDate fromDate,
            LocalDate toDate,
            BiFunction<LocalDate, Long, R> mapper
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<T> root = query.from(entityClass);

        Expression<Instant> bucket = cb.function(
                "date_trunc", Instant.class, cb.literal(unit), root.get("createdAt")
        );

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(notDeleted(root, cb));
        if (filterFn != null) {
            predicates.add(filterFn.apply(root, cb));
        }
        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    root.get("createdAt"), fromDate.atStartOfDay(APPLICATION_ZONE).toInstant()
            ));
        }
        if (toDate != null) {
            predicates.add(cb.lessThan(
                    root.get("createdAt"), toDate.plusDays(1).atStartOfDay(APPLICATION_ZONE).toInstant()
            ));
        }

        query.select(cb.tuple(bucket, cb.count(root)))
                .where(cb.and(predicates.toArray(Predicate[]::new)))
                .groupBy(bucket)
                .orderBy(cb.asc(bucket));

        return entityManager.createQuery(query).getResultList().stream()
                .map(tuple -> mapper.apply(
                        ((Instant) tuple.get(0)).atZone(APPLICATION_ZONE).toLocalDate(),
                        (Long) tuple.get(1)
                ))
                .toList();
    }

    private Predicate combinedPredicate(
            Root<T> root,
            CriteriaBuilder cb,
            BiFunction<Root<T>, CriteriaBuilder, Predicate> filterFn,
            Predicate... extra
    ) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(notDeleted(root, cb));
        if (filterFn != null) {
            predicates.add(filterFn.apply(root, cb));
        }
        predicates.addAll(List.of(extra));

        return cb.and(predicates.toArray(Predicate[]::new));
    }

    @SuppressWarnings("unchecked")
    private <D, R> List<R> runAndMap(CriteriaQuery<Tuple> query, Integer limit, BiFunction<D, Long, R> mapper) {
        TypedQuery<Tuple> typedQuery = entityManager.createQuery(query);
        if (limit != null) {
            typedQuery.setMaxResults(limit);
        }

        return typedQuery.getResultList().stream()
                .map(tuple -> mapper.apply((D) tuple.get(0), (Long) tuple.get(1)))
                .toList();
    }
}
