package uz.uzinfocom.app.modules.card.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.card.application.exception.CardNotFoundException;
import uz.uzinfocom.app.modules.card.application.exception.CardScopeViolationException;
import uz.uzinfocom.app.modules.card.application.exception.CardValidationException;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandler;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandlerRegistry;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTableResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.detail.CardDetailResponse;
import uz.uzinfocom.app.modules.card.application.query.dto.pdf.CardPdfResponse;
import uz.uzinfocom.app.modules.card.application.query.mapper.CardTableMapper;
import uz.uzinfocom.app.modules.card.application.query.projection.CardTableProjection;
import uz.uzinfocom.app.modules.card.domain.enums.CaseFormType;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardRepository;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.specification.CardCaseScopeSpecification;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.specification.CardSpecification;
import uz.uzinfocom.app.modules.form058.application.query.mapper.Form058PdfMapper;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.orchestration.scope.jpa.SenderReceiverScopePredicateFactory;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;
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
    private final Form058PdfMapper form058PdfMapper;

    @Transactional(readOnly = true)
    public Page<CardTableResponse> findTable(CardFilterRequest filter) {
        return queryTable(CardSpecification.byFilter(filter), PageableUtils.of(filter, CardSortFields.ALLOWED));
    }

    /**
     * Organization-scoped listing behind {@code GET /v1/cards} — every card
     * visible within the caller's organization scope (see {@link
     * CardCaseScopeSpecification}), regardless of who it's assigned to.
     * Unlike {@link #findMine}, this never narrows to the attached employee:
     * {@code assignedToUserId}/{@code assignedById} stay caller-supplied
     * optional filters within that scope, not a forced identity.
     */
    @Transactional(readOnly = true)
    public Page<CardTableResponse> findAll(CardFilterRequest filter) {
        ResolvedOrganizationScope scope = currentScope();

        Specification<Card> spec = CardSpecification.byFilter(filter)
                .and((root, query, cb) -> CardCaseScopeSpecification.scopePredicate(
                        root, cb, scopePredicateFactory, scope, CaseFormType.ANY));

        return queryTable(spec, PageableUtils.of(filter, CardSortFields.ALLOWED));
    }

    /**
     * The attached employee's own view — {@code assignedToUserId} is always
     * forced to the authenticated user, mirroring {@code ActQueryService}'s
     * personal branch. Does not widen for broader-scope (region/republic)
     * organizations — that org-wide view now lives in {@link #findAll}
     * instead, so "mine" stays personal for every account.
     */
    @Transactional(readOnly = true)
    public Page<CardTableResponse> findMine(CardFilterRequest filter) {
        Specification<Card> spec = CardSpecification.byFilter(filter.scopedToAttachedUser(requireCurrentUserId()));
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

    private ResolvedOrganizationScope currentScope() {
        Organization organization = CurrentOrganizationContext.getOptional()
                .orElseThrow(CardScopeViolationException::new);
        return organizationScopeResolver.resolve(organization);
    }

    @Transactional(readOnly = true)
    public CardDetailResponse getById(Long id) {
        Card card = cardRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        CardTypeHandler<?, ?, ?> handler = handlerRegistry.get(card.getCardType());
        return handler.handleToResponse(card);
    }

    /**
     * Same per-type card fields as {@link #getById} (raw catalog codes - each
     * card type has its own frontend print template that already knows how to
     * resolve them), plus the linked Form058 in its print-oriented shape
     * ({@link Form058PdfMapper}) - every card's print form has a header with
     * the patient's identity, address and workplace/school, none of which
     * live on the card itself.
     */
    @Transactional(readOnly = true)
    public CardPdfResponse getPdf(Long id) {
        Card card = cardRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        if (card.isAttachedToForm0581()) {
            // PDF export for form0581-owned cards isn't built yet — Form0581PdfMapper
            // doesn't exist (see Card.java javadoc); left as a follow-up.
            throw new CardValidationException("error.card.pdf-not-supported-for-form0581");
        }

        CardTypeHandler<?, ?, ?> handler = handlerRegistry.get(card.getCardType());
        CardDetailResponse cardResponse = handler.handleToResponse(card);

        CardFilterRequest linkedCardsFilter = new CardFilterRequest(
                1, 200, null, null, card.getForm058().getId(), null, null, null, null, null
        );
        List<CardTableResponse> linkedCards = findTable(linkedCardsFilter).getContent();

        return new CardPdfResponse(cardResponse, form058PdfMapper.toPdfResponse(card.getForm058(), linkedCards));
    }

    private Long requireCurrentUserId() {
        Long userId = currentUserProvider.userIdOrNull();
        if (userId == null) {
            throw new CardScopeViolationException();
        }
        return userId;
    }
}
