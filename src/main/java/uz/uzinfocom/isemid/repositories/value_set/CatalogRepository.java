package uz.uzinfocom.isemid.repositories.value_set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CatalogRepository extends JpaRepository<Catalog, Long>, JpaSpecificationExecutor<Catalog> {

    List<Catalog> findAllByCode(String code);

    List<Catalog> findAllByTypeAndParentCode(String catalogType, String parentCode);

    List<Catalog> findAllByType(String catalogType);

    @Query(value = """
    SELECT * FROM catalog c
    WHERE c.type = :type
      AND (:search IS NULL
           OR LOWER(c.name_ru) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(c.name_uz) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(c.name_uz_cyril) LIKE LOWER(CONCAT('%', :search, '%')))
    ORDER BY 
        CASE 
            WHEN LOWER(c.code) = 'uzb' THEN 1
            WHEN LOWER(c.code) = '207' THEN 2
            WHEN LOWER(c.code) = 'rus' THEN 3
            WHEN LOWER(c.code) = '226' THEN 4
            ELSE 3
        END,
        c.name_uz ASC
    LIMIT 20
    """, nativeQuery = true)
    List<Catalog> findByTypeAndSearch(@Param("type") String type, @Param("search") String search);


    @Query(value = """
    SELECT * FROM catalog c
    WHERE c.type = :type
      AND (:search IS NULL
           OR LOWER(c.name_ru) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(c.name_uz) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(c.name_kaa) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(c.name_uz_cyril) LIKE LOWER(CONCAT('%', :search, '%')))
     ORDER BY 
            c.name_uz ASC,
            c.name_ru ASC,
            c.name_kaa ASC
    LIMIT 20
    """, nativeQuery = true)
    List<Catalog> findByProfessionTypeAndSearch(@Param("type") String type, @Param("search") String search);

    Optional<Catalog> findByTypeAndParentCode(String catalogType, String parentCode);

    Optional<Catalog> findByTypeAndCode(String name, String code);

    @Query("SELECT c.code FROM Catalog c")
    Set<String> findAllCodes();

}
