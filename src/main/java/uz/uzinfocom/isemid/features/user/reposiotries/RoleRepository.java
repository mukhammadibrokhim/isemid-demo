package uz.uzinfocom.isemid.features.user.reposiotries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.user.models.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    Optional<Role> findByName(String name);

    @Query(value = """
                SELECT * FROM role r
                WHERE\s
                    LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(r.description_uz) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(r.description_uz_cyril) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(r.description_ru) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(r.description_kaa) LIKE LOWER(CONCAT('%', :search, '%'))
                ORDER BY r.name
                LIMIT 10
           \s""", nativeQuery = true)
    List<Role> searchRolesNative(@Param("search") String search);

}
