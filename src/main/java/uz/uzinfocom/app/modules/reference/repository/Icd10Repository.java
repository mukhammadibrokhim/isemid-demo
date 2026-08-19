package uz.uzinfocom.app.modules.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.modules.reference.application.icd10.query.projection.Icd10ChildrenCountProjection;
import uz.uzinfocom.app.modules.reference.domain.Icd10;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface Icd10Repository extends JpaRepository<Icd10, Long>, JpaSpecificationExecutor<Icd10> {

    Optional<Icd10> findByIdAndDeletedFalse(Long id);

    Optional<Icd10> findByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    List<Icd10> findAllByParentIsNullAndDeletedFalseOrderByCodeAsc();

    List<Icd10> findAllByParent_IdAndDeletedFalseOrderByCodeAsc(Long parentId);

    long countByParent_IdAndDeletedFalse(Long parentId);

    // Batches the children-count lookup for a whole page of nodes into a single
    // GROUP BY query, instead of one COUNT(*) per node - see Icd10QueryService
    // (getRoots/getChildren used to N+1 here, which was the main source of
    // multi-second latency on those two endpoints).
    @Query("""
            select c.parentId as parentId, count(c) as childrenCount
            from Icd10 c
            where c.parentId in :parentIds and c.deleted = false
            group by c.parentId
            """)
    List<Icd10ChildrenCountProjection> countChildrenByParentIds(@Param("parentIds") Collection<Long> parentIds);
}
