package uz.uzinfocom.isemid.features.classifier.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.classifier.dtos.Mkb10Projection;
import uz.uzinfocom.isemid.features.classifier.models.Mkb10;

import java.util.List;
import java.util.Optional;
import java.util.Set;


@Repository
public interface Mkb10Repository extends JpaRepository<Mkb10, Long>, JpaSpecificationExecutor<Mkb10> {
    @Query(value = """
             SELECT * FROM public.cls_mkb10\s
             WHERE (
                 COALESCE(:search, '') = ''\s
                 AND (:parentId IS NULL AND parent_id IS NULL OR parent_id = :parentId)
             ) OR (
                 COALESCE(:search, '') <> '' AND (
                     LOWER(code) LIKE LOWER(CONCAT('%', :search,'%'))\s
                     OR LOWER(name_ru) LIKE LOWER(CONCAT('%', :search,'%'))\s
                     OR LOWER(name_uz) LIKE LOWER(CONCAT('%', :search,'%'))\s
                     OR LOWER(name_uz_cyril) LIKE LOWER(CONCAT('%', :search,'%'))\s
                     OR LOWER(name_kaa) LIKE LOWER(CONCAT('%', :search,'%'))
                 )
             )
            \s""", nativeQuery = true)
    List<Mkb10> searchMkb10(@Param("parentId") Long parentId, @Param("search") String search);

    List<Mkb10> findAllByCodeIn(Set<String> codes);

    @Query("SELECT c.code FROM Mkb10 c")
    Set<String> findAllCodes();

    Optional<Mkb10> findByCode(String code);

    Optional<Mkb10> findById(Long id);

    @Query("""
                SELECT new uz.uzinfocom.isemid.features.classifier.dtos.Mkb10Projection(
                    m.code,
                    CASE
                        WHEN :locale = 'ru' THEN m.nameRu
                        WHEN :locale = 'kaa' THEN m.nameKaa
                        WHEN :locale = 'uz_cyrl' THEN m.nameUzCyril
                        ELSE m.nameUz
                    END
                )
                FROM Mkb10 m
                WHERE m.code IN :codes
            """)
    List<Mkb10Projection> findNamesByCodesAndLocale(@Param("codes") List<String> codes,
                                                    @Param("locale") String locale);
}
