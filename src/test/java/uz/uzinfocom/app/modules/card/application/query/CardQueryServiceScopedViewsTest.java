package uz.uzinfocom.app.modules.card.application.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.app.modules.card.application.exception.CardScopeViolationException;
import uz.uzinfocom.app.modules.card.application.handler.CardTypeHandlerRegistry;
import uz.uzinfocom.app.modules.card.application.query.mapper.CardTableMapper;
import uz.uzinfocom.app.modules.card.application.shared.CurrentCardUser;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.repository.CardRepository;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code CardFilterRequestTest} covers the actual scoping logic (pure,
 * mockless). This test only pins down the auth gate: both "self-scoped"
 * views must refuse to run at all for an unauthenticated caller rather than
 * silently falling back to an unscoped query.
 */
class CardQueryServiceScopedViewsTest {

    private CardRepository cardRepository;
    private CurrentCardUser currentCardUser;
    private CardQueryService service;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        cardRepository = mock(CardRepository.class);
        CardTableMapper cardTableMapper = mock(CardTableMapper.class);
        CardTypeHandlerRegistry handlerRegistry = mock(CardTypeHandlerRegistry.class);
        currentCardUser = mock(CurrentCardUser.class);

        service = new CardQueryService(cardRepository, cardTableMapper, handlerRegistry, currentCardUser);

        when(cardRepository.findBy(any(Specification.class), any(Function.class)))
                .thenReturn(Page.empty());
    }

    @Test
    void findMineRunsForAnAuthenticatedUser() {
        when(currentCardUser.userIdOrNull()).thenReturn(42L);

        service.findMine(emptyFilter());

        verify(cardRepository, times(1)).findBy(any(Specification.class), any(Function.class));
    }

    @Test
    void findMineRefusesAnUnauthenticatedCaller() {
        when(currentCardUser.userIdOrNull()).thenReturn(null);

        assertThatThrownBy(() -> service.findMine(emptyFilter()))
                .isInstanceOf(CardScopeViolationException.class);
    }

    @Test
    void findPendingSupervisorApprovalRunsForAnAuthenticatedUser() {
        when(currentCardUser.userIdOrNull()).thenReturn(42L);

        service.findPendingSupervisorApproval(emptyFilter());

        verify(cardRepository, times(1)).findBy(any(Specification.class), any(Function.class));
    }

    @Test
    void findPendingSupervisorApprovalRefusesAnUnauthenticatedCaller() {
        when(currentCardUser.userIdOrNull()).thenReturn(null);

        assertThatThrownBy(() -> service.findPendingSupervisorApproval(emptyFilter()))
                .isInstanceOf(CardScopeViolationException.class);
    }

    private CardFilterRequest emptyFilter() {
        return new CardFilterRequest(1, 20, null, null, null, null, null, null, null);
    }
}
