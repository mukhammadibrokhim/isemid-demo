package uz.uzinfocom.app.modules.card.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import uz.uzinfocom.app.modules.card.application.shared.CurrentCardUser;
import uz.uzinfocom.app.modules.card.domain.model.Card;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardRepository;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.specification.CardSpecification;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CardQueryService {

    private final CardRepository cardRepository;
    private final CardTableMapper cardTableMapper;
    private final CardTypeHandlerRegistry handlerRegistry;
    private final CurrentCardUser currentCardUser;

    @Transactional(readOnly = true)
    public Page<CardTableResponse> findTable(CardFilterRequest filter) {
        Pageable pageable = PageableUtils.of(filter, CardSortFields.ALLOWED);

        Page<CardTableProjection> page = Objects.requireNonNull(cardRepository.findBy(
                CardSpecification.byFilter(filter),
                query ->
                        query.as(CardTableProjection.class)
                                .page(pageable)), "Card table page returned null"
        );

        return page.map(cardTableMapper::toTableResponse);
    }

    /**
     * The attached employee's own view — {@code assignedToUserId} is
     * always forced to the authenticated user, regardless of what the
     * caller passed in, so nobody can browse another employee's queue by
     * guessing their id.
     */
    @Transactional(readOnly = true)
    public Page<CardTableResponse> findMine(CardFilterRequest filter) {
        return findTable(filter.scopedToAttachedUser(requireCurrentUserId()));
    }

    /**
     * The supervisor's review queue — cards assigned to the authenticated
     * user that are COMPLETED and awaiting an approve/reject decision.
     */
    @Transactional(readOnly = true)
    public Page<CardTableResponse> findPendingSupervisorApproval(CardFilterRequest filter) {
        return findTable(filter.scopedToSupervisor(requireCurrentUserId()));
    }

    @Transactional(readOnly = true)
    public CardDetailResponse getById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        CardTypeHandler<?, ?, ?> handler = handlerRegistry.get(card.getCardType());
        return handler.handleToResponse(card);
    }

    private Long requireCurrentUserId() {
        Long userId = currentCardUser.userIdOrNull();
        if (userId == null) {
            throw new CardScopeViolationException();
        }
        return userId;
    }
}
