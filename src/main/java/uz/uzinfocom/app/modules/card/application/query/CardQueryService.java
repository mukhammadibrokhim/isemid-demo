package uz.uzinfocom.app.modules.card.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.card.application.exception.CardNotFoundException;
import uz.uzinfocom.app.modules.card.application.exception.CardScopeViolationException;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandler;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandlerRegistry;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTableResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.CardDetailResponse;
import uz.uzinfocom.app.modules.card.application.query.mapper.CardTableMapper;
import uz.uzinfocom.app.modules.card.application.query.projection.CardTableProjection;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardRepository;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.specification.CardSpecification;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.scope.jpa.SenderReceiverScopePredicateFactory;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CardQueryService {

    private final CardRepository cardRepository;
    private final CardTableMapper cardTableMapper;
    private final CardTypeHandlerRegistry handlerRegistry;
    private final CurrentUserProvider currentUserProvider;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final SenderReceiverScopePredicateFactory scopePredicateFactory;

    @Transactional(readOnly = true)
    public Page<CardTableResponse> findTable(CardFilterRequest filter) {
        return queryTable(CardSpecification.byFilter(filter), PageableUtils.of(filter, CardSortFields.ALLOWED));
    }

    /**
     * The attached employee's own view by default — {@code assignedToUserId}
     * is forced to the authenticated user so nobody can browse another
     * employee's queue by guessing their id. For a broader-scope
     * organization (region/republic-level SANEPID, see
     * {@link OrganizationScopeResolver}), there is no single "attached
     * employee" to scope by — the account represents an oversight body, not
     * a case worker — so the view widens instead to every card visible
     * within that organization's scope (mirroring how Form058's own
     * ALL/REGION scope views work), without forcing a user filter.
     */
    @Transactional(readOnly = true)
    public Page<CardTableResponse> findMine(CardFilterRequest filter) {
        ResolvedOrganizationScope scope = currentScope();

        Specification<Card> spec = isBroaderScope(scope)
                ? CardSpecification.byFilter(filter)
                        .and((root, query, cb) -> scopePredicateFactory.applyDirectionScope(root.get("form058"), cb, scope, true))
                : CardSpecification.byFilter(filter.scopedToAttachedUser(requireCurrentUserId()));

        return queryTable(spec, PageableUtils.of(filter, CardSortFields.ALLOWED));
    }

    private Page<CardTableResponse> queryTable(Specification<Card> spec, Pageable pageable) {
        Page<CardTableProjection> page = Objects.requireNonNull(cardRepository.findBy(
                spec,
                query ->
                        query.as(CardTableProjection.class)
                                .page(pageable)), "Card table page returned null"
        );

        return page.map(cardTableMapper::toTableResponse);
    }

    /**
     * REGION/ALL are the "higher-standing organization" tiers (regional and
     * republican SANEPID headquarters) that oversee many case workers across
     * many organizations — for those, "mine" means the whole scope.
     * DISTRICT/ORGANIZATION are a concrete office where individual employees
     * are actually assigned cards — for those, "mine" stays personal.
     */
    private boolean isBroaderScope(ResolvedOrganizationScope scope) {
        return scope.mode() == OrganizationScopeMode.REGION || scope.mode() == OrganizationScopeMode.ALL;
    }

    private ResolvedOrganizationScope currentScope() {
        Organization organization = CurrentOrganizationContext.getOptional()
                .orElseThrow(CardScopeViolationException::new);
        return organizationScopeResolver.resolve(organization);
    }

    @Transactional(readOnly = true)
    public CardDetailResponse getById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        CardTypeHandler<?, ?, ?> handler = handlerRegistry.get(card.getCardType());
        return handler.handleToResponse(card);
    }

    private Long requireCurrentUserId() {
        Long userId = currentUserProvider.userIdOrNull();
        if (userId == null) {
            throw new CardScopeViolationException();
        }
        return userId;
    }
}
