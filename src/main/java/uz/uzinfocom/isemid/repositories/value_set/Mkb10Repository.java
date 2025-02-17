package uz.uzinfocom.isemid.repositories.value_set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.models.entities.value_set.Mkb10;

import java.util.List;


@Repository
public interface Mkb10Repository extends JpaRepository<Mkb10, Long>, JpaSpecificationExecutor<Mkb10> {
    List<Mkb10> findAllByCode(String code);

    @Query(value = """
        SELECT * FROM mkb10 
        WHERE (
            COALESCE(:search, '') = '' 
            AND (:parentId IS NULL AND parent_id IS NULL OR parent_id = :parentId)
        ) OR (
            COALESCE(:search, '') <> '' AND (
                LOWER(code) LIKE LOWER(CONCAT('%', :search, '%')) 
                OR LOWER(name_ru) LIKE LOWER(CONCAT('%', :search, '%')) 
                OR LOWER(name_uz) LIKE LOWER(CONCAT('%', :search, '%')) 
                OR LOWER(name_uz_cyril) LIKE LOWER(CONCAT('%', :search, '%')) 
                OR LOWER(name_kaa) LIKE LOWER(CONCAT('%', :search, '%'))
            )
        )
        """, nativeQuery = true)
    List<Mkb10> searchMkb10(@Param("parentId") Long parentId, @Param("search") String search);

}
