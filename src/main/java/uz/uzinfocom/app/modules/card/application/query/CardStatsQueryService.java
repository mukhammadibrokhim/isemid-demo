package uz.uzinfocom.app.modules.card.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.card.application.exception.CardScopeViolationException;
import uz.uzinfocom.app.modules.card.application.query.dto.CardStatusCountResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTypeCountResponse;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardStatsRepository;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;

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

    public List<CardStatusCountResponse> countByStatus() {
        return cardStatsRepository.countByStatus(currentScope());
    }

    public List<CardTypeCountResponse> countByType() {
        return cardStatsRepository.countByType(currentScope());
    }

    private ResolvedOrganizationScope currentScope() {
        Organization currentOrganization = CurrentOrganizationContext.getOptional()
                .orElseThrow(CardScopeViolationException::new);

        return organizationScopeResolver.resolve(currentOrganization);
    }
}
