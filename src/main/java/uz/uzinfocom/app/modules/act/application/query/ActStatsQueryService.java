package uz.uzinfocom.app.modules.act.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.act.application.exception.ActScopeViolationException;
import uz.uzinfocom.app.modules.act.application.query.dto.ActStatusCountResponse;
import uz.uzinfocom.app.modules.act.infrastructure.persistence.repository.ActStatsRepository;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.util.List;

/**
 * Public query surface for act statistics — the module boundary other
 * platform code (e.g. the home dashboard) must go through instead of
 * reaching into {@link ActStatsRepository} directly.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActStatsQueryService {

    private final ActStatsRepository actStatsRepository;
    private final OrganizationScopeResolver organizationScopeResolver;

    public List<ActStatusCountResponse> countByStatus() {
        return actStatsRepository.countByStatus(currentScope());
    }

    private ResolvedOrganizationScope currentScope() {
        Organization currentOrganization = CurrentOrganizationContext.getOptional()
                .orElseThrow(ActScopeViolationException::new);

        return organizationScopeResolver.resolve(currentOrganization);
    }
}
