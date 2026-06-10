package uz.uzinfocom.app.platform.iam.application.shared.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.platform.iam.application.shared.dto.AuditUserResponse;
import uz.uzinfocom.app.platform.iam.domain.User;

import java.util.Optional;

public interface AuditUserQueryRepository extends Repository<User, Long> {

    @Query("""
            select new uz.uzinfocom.app.platform.iam.application.shared.dto.AuditUserResponse(
                u.id,
                u.firstName,
                u.lastName,
                u.middleName
            )
            from User u
            where u.id = :userId
            """)
    Optional<AuditUserResponse> findAuditUserById(
            @Param("userId") Long userId
    );
}