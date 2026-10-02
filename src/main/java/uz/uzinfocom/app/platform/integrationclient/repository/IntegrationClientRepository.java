package uz.uzinfocom.app.platform.integrationclient.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationAuthType;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;

import java.util.List;
import java.util.Optional;

public interface IntegrationClientRepository
        extends JpaRepository<IntegrationClient, Long>, JpaSpecificationExecutor<IntegrationClient> {

    Optional<IntegrationClient> findByClientId(String clientId);

    boolean existsByClientId(String clientId);

    boolean existsBySourceKey(String sourceKey);

    List<IntegrationClient> findAllByAuthTypeAndActiveTrue(IntegrationAuthType authType);

    @Query("""
        select distinct c.sourceKey
        from IntegrationClient c
        where c.active = true
          and (:search = '' or lower(c.sourceKey) like concat('%', :search, '%'))
        order by c.sourceKey asc
        """)
    List<String> findDistinctActiveSourceKeys(@Param("search") String search, Pageable pageable);
}
