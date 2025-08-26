package uz.uzinfocom.isemid.features.user.reposiotries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uzinfocom.isemid.features.user.models.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUuid(UUID uuid);

    Optional<User> findByNnuzb(String nnuzb);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM user_roles ur " +
            "JOIN users u ON ur.user_id = u.id " +
            "JOIN role r ON ur.role_id = r.id " +
            "WHERE r.name = :roleName AND u.id != :currentUserId", nativeQuery = true)
    boolean existsSuperAdminForOtherUser(@Param("roleName") String roleName,
                                         @Param("currentUserId") Long currentUserId);


    @Query(value = """
        SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END
        FROM user_roles ur
        JOIN role r ON ur.role_id = r.id
        WHERE r.name = :roleName
        """, nativeQuery = true)
    boolean existsByRoleName(String roleName);
}
