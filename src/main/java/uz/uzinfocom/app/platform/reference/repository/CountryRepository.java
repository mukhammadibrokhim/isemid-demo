package uz.uzinfocom.app.platform.reference.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.uzinfocom.app.platform.reference.application.lookup.projection.ReferenceItemProjection;
import uz.uzinfocom.app.platform.reference.domain.Country;

import java.util.List;
import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {

    Optional<Country> findByIdAndDeletedFalse(Long id);

    Optional<Country> findByCodeAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalse(String code);

    boolean existsByCode(String code);

    List<Country> findAllByDeletedFalseOrderBySortOrderAscNameUzAsc();

    @Query("""
        select
            c.code as code,
            c.nameUz as nameUz,
            c.nameUzCyril as nameUzCyril,
            c.nameRu as nameRu,
            c.nameKaa as nameKaa
        from Country c
        where c.deleted = false
        order by c.sortOrder asc, c.nameUz asc
    """)
    List<ReferenceItemProjection> findAllReferenceItems();
}
