package uz.uzinfocom.isemid.features.act.repositores;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.act.models.Act;

@Repository
public interface ActRepository extends JpaRepository<Act, Long>, JpaSpecificationExecutor<Act> {

    List<Act> findAllByCardId(Long cardId);

    /**
     * Loads an Act together with its type-specific detail collections in a single query,
     * avoiding the N+1 problem that would occur when accessing lazy-loaded collections
     * after a plain {@code findById}.
     *
     * <p>TREAT expressions let Hibernate LEFT JOIN the subtype-specific association
     * tables only for the matching concrete type; non-matching subtypes produce no
     * extra rows.  DISTINCT collapses the Cartesian product back to one Act root.
     */
    @Query("""
        SELECT DISTINCT a FROM Act a
        LEFT JOIN FETCH TREAT(a AS Act153).act153Details
        LEFT JOIN FETCH TREAT(a AS Act154).act154Details
        LEFT JOIN FETCH TREAT(a AS Act223).act223Details
        WHERE a.id = :id
        """)
    Optional<Act> findWithDetailsForLisById(@Param("id") Long id);
}
