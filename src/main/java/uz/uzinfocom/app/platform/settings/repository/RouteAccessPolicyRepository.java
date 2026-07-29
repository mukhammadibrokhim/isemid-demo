package uz.uzinfocom.app.platform.settings.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.uzinfocom.app.platform.settings.domain.RouteAccessPolicy;

import java.util.List;

public interface RouteAccessPolicyRepository
        extends JpaRepository<RouteAccessPolicy, Long>, JpaSpecificationExecutor<RouteAccessPolicy> {

    boolean existsByPatternIgnoreCase(String pattern);

    List<RouteAccessPolicy> findByEnabledTrueOrderByDisplayOrderAsc();
}
