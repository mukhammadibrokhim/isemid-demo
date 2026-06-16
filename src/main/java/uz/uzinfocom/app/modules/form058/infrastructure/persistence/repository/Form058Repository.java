package uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.projection.Form058StatusCountProjection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Form058Repository extends JpaRepository<Form058, Long>, JpaSpecificationExecutor<Form058> {

    Optional<Form058> findByUuid(UUID uuid);

    boolean existsByIdAndHasLinkedCardsTrue(Long id);

    @Query("""
            select f.status as status, count(f.id) as count
            from Form058 f
            where f.senderOrganizationId = :organizationId
               or f.receiverOrganizationId = :organizationId
            group by f.status
            """)
    List<Form058StatusCountProjection> countStatusesByOrganization(@Param("organizationId") Long organizationId);
}
