package uz.uzinfocom.app.platform.integrationclient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.platform.integrationclient.domain.IntegrationClient;

import java.util.Optional;

public interface IntegrationClientRepository
        extends JpaRepository<IntegrationClient, Long>, JpaSpecificationExecutor<IntegrationClient> {

    Optional<IntegrationClient> findByClientId(String clientId);

    boolean existsByClientId(String clientId);

    boolean existsBySourceKey(String sourceKey);
}
