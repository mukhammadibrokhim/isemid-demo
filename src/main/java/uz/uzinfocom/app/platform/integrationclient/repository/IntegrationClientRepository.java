package uz.uzinfocom.app.platform.integrationclient.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IntegrationClientRepository
        extends JpaRepository<IntegrationClient, Long>, JpaSpecificationExecutor<IntegrationClient> {

    Optional<IntegrationClient> findByClientId(String clientId);

    boolean existsByClientId(String clientId);

    boolean existsBySourceKey(String sourceKey);

    List<IntegrationClient> findAllByAuthTypeAndActiveTrue(IntegrationAuthType authType);

    Optional<IntegrationClient> findByProviderKeyAndExternalClientIdAndAuthTypeAndActiveTrue(
            String providerKey, String externalClientId, IntegrationAuthType authType);

    boolean existsByProviderKeyAndExternalClientId(String providerKey, String externalClientId);

    /**
     * Bulk JPQL update, so it neither bumps {@code @Version} (a concurrent burst of
     * requests from one client no longer races on optimistic locking) nor runs at all
     * when the stored value is still fresher than {@code staleBefore}.
     */
    @Transactional
    @Modifying
    @Query("""
        update IntegrationClient c
        set c.lastUsedAt = :usedAt
        where c.id = :id
          and (c.lastUsedAt is null or c.lastUsedAt < :staleBefore)
        """)
    int touchLastUsedAt(@Param("id") Long id, @Param("usedAt") Instant usedAt, @Param("staleBefore") Instant staleBefore);

    @Query("""
        select distinct c.sourceKey
        from IntegrationClient c
        where c.active = true
          and (:search = '' or lower(c.sourceKey) like concat('%', :search, '%'))
        order by c.sourceKey asc
        """)
    List<String> findDistinctActiveSourceKeys(@Param("search") String search, Pageable pageable);
}
