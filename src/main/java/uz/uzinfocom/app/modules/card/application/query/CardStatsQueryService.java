package uz.uzinfocom.app.modules.card.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.card.application.exception.CardScopeViolationException;
import uz.uzinfocom.app.modules.card.application.query.dto.CardDailyCountResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.CardStatusCountResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTypeCountResponse;
import uz.uzinfocom.app.modules.card.domain.enums.CaseFormType;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardStatsRepository;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

import java.time.LocalDate;
import java.util.List;

/**
 * Public query surface for card statistics — the module boundary other
 * platform code (e.g. the home dashboard) must go through instead of
 * reaching into {@link CardStatsRepository} directly. Cards only ever exist
 * on the receiving (investigating) side of a case, so there is no
 * direction parameter, unlike {@code Form058StatsQueryService}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardStatsQueryService {

    private final CardStatsRepository cardStatsRepository;
    private final OrganizationScopeResolver organizationScopeResolver;

    /** Year-to-date, across both form058- and form0581-owned cards — the standalone card dashboard's breakdown. */
    public List<CardStatusCountResponse> countByStatus() {
        return countByStatus(CaseFormType.ANY);
    }

    /** Year-to-date, restricted to cards owned by one case type — used to embed a per-form breakdown in that form's own dashboard. */
    public List<CardStatusCountResponse> countByStatus(CaseFormType formType) {
        return cardStatsRepository.countByStatus(currentScope(), formType);
    }

    public List<CardTypeCountResponse> countByType() {
        return cardStatsRepository.countByType(currentScope());
    }

    public List<CardDailyCountResponse> countByMonth(LocalDate from, LocalDate to) {
        return cardStatsRepository.countByMonth(currentScope(), from, to);
    }

    /** Year-to-date, across both form058- and form0581-owned cards. */
    public long countTotal() {
        return countTotal(CaseFormType.ANY);
    }

    /** Year-to-date, restricted to cards owned by one case type. */
    public long countTotal(CaseFormType formType) {
        return cardStatsRepository.countTotal(currentScope(), formType);
    }

    /** Year-to-date, across both form058- and form0581-owned cards. */
    public long countActive() {
        return countActive(CaseFormType.ANY);
    }

    /** Year-to-date, restricted to cards owned by one case type. */
    public long countActive(CaseFormType formType) {
        return cardStatsRepository.countActive(currentScope(), formType);
    }

    private ResolvedOrganizationScope currentScope() {
        Organization currentOrganization = CurrentOrganizationContext.getOptional()
                .orElseThrow(CardScopeViolationException::new);

        return organizationScopeResolver.resolve(currentOrganization);
    }
}
