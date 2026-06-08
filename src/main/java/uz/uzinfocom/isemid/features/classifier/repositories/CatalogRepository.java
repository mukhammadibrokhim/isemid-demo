package uz.uzinfocom.isemid.features.classifier.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.classifier.models.Catalog;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CatalogRepository extends JpaRepository<Catalog, Long>, JpaSpecificationExecutor<Catalog> {

    List<Catalog> findAllByTypeAndParentCode(String catalogType, String parentCode);

    List<Catalog> findAllByType(String catalogType);

    @Query("""
                SELECT c FROM Catalog c
                WHERE c.type = :catalogType
                  AND (
                        :search IS NULL OR :search = ''
                        OR LOWER(c.nameUz) LIKE LOWER(CONCAT('%', :search, '%'))
                        OR LOWER(c.nameRu) LIKE LOWER(CONCAT('%', :search, '%'))
                        OR LOWER(c.nameUzCyril) LIKE LOWER(CONCAT('%', :search, '%'))
                        OR LOWER(c.nameKaa) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """)
    List<Catalog> searchByTypeAndName(
            @Param("catalogType") String catalogType,
            @Param("search") String search
    );


    List<Catalog> findAllByParentCode(String parentCode);

    Optional<Catalog> findByCode(String code);

    @Query(value = """
    SELECT * FROM cls_catalog c
    WHERE c.type = :type
      AND (:search IS NULL
           OR LOWER(c.name_ru) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(c.name_uz) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(c.name_uz_cyril) LIKE LOWER(CONCAT('%', :search, '%')))
    ORDER BY\s
        CASE\s
            WHEN LOWER(c.code) = 'uzb' THEN 1
            WHEN LOWER(c.code) = '207' THEN 2
            WHEN LOWER(c.code) = 'rus' THEN 3
            WHEN LOWER(c.code) = '226' THEN 4
            ELSE 3
        END,
        c.name_uz
    LIMIT 20
   \s""", nativeQuery = true)
    List<Catalog> findByTypeAndSearch(@Param("type") String type, @Param("search") String search);

    @Query("SELECT c.code FROM Catalog c")
    Set<String> findAllCodes();

    @Query("""
                SELECT c FROM Catalog c\s
                LEFT JOIN FETCH Catalog p ON c.parentCode = p.code
                WHERE (:type IS NULL OR c.type = :type)
           \s""")
    Page<Catalog> findAllWithParent(@Param("type") String type, Pageable pageable);

    Optional<Catalog> findByTypeAndParentCode(String catalogType, String parentCode);

    Optional<Catalog> findByTypeAndCode(String name, String code);
    List<Catalog> findByTypeAndCodeLike(String name, String code);

    @Query("""
                SELECT\s
                    CASE
                        WHEN :locale = 'ru' THEN c.nameRu
                        WHEN :locale = 'kaa' THEN c.nameKaa
                        WHEN :locale = 'uz_cyrl' THEN c.nameUzCyril
                        ELSE c.nameUz
                    END
                FROM Catalog c
                WHERE LOWER(c.type) = 'state' AND c.code = :code
           \s""")
    Optional<String> findRegionNameByCodeAndLocale(@Param("code") String code,
                                                   @Param("locale") String locale);
}
