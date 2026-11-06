package uz.uzinfocom.app.modules.act.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.act.application.exception.ActScopeViolationException;
import uz.uzinfocom.app.modules.act.application.query.dto.ActDailyCountResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.ActStatusCountResponse;
import uz.uzinfocom.app.modules.act.infrastructure.persistence.repository.ActStatsRepository;
import uz.uzinfocom.app.modules.card.domain.enums.CaseFormType;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.time.LocalDate;
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

    /** Year-to-date, restricted to acts whose card is owned by one case type — used to embed a per-form breakdown in that form's own dashboard. */
    public List<ActStatusCountResponse> countByStatus(CaseFormType formType) {
        return actStatsRepository.countByStatus(currentScope(), formType);
    }

    public List<ActDailyCountResponse> countByMonth(LocalDate from, LocalDate to) {
        return actStatsRepository.countByMonth(currentScope(), from, to);
    }

    /** Year-to-date, restricted to acts whose card is owned by one case type. */
    public long countTotal(CaseFormType formType) {
        return actStatsRepository.countTotal(currentScope(), formType);
    }

    private ResolvedOrganizationScope currentScope() {
        Organization currentOrganization = CurrentOrganizationContext.getOptional()
                .orElseThrow(ActScopeViolationException::new);

        return organizationScopeResolver.resolve(currentOrganization);
    }
}
