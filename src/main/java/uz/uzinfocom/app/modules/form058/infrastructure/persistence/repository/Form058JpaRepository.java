package uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;

import java.util.Optional;

public interface Form058JpaRepository extends JpaRepository<Form058, Long>, JpaSpecificationExecutor<Form058> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT f
            FROM Form058 f
            WHERE f.id = :id
              AND f.deleteInfo.deleted = false
            """)
    Optional<Form058> findByIdAndDeletedFalse(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT f
            FROM Form058 f
            WHERE f.id = :id
              AND f.deleteInfo.deleted = false
            """)
    Optional<Form058> findActiveByIdForUpdate(@Param("id") Long id);

    /**
     * Planner-only row estimate for the unfiltered active-row predicate, used to avoid an
     * exact COUNT(*) scan over the whole table when a list query has no real narrowing
     * predicate (see ExplainRowCountEstimator). Table name is a compile-time constant here,
     * not user input, so this native query carries no injection surface.
     */
    @Query(value = "EXPLAIN (FORMAT JSON) SELECT 1 FROM form058 WHERE deleted = false", nativeQuery = true)
    String explainActiveRowCountPlan();

}
