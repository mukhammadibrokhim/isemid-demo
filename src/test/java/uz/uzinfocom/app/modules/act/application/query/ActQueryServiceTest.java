package uz.uzinfocom.app.modules.act.application.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import uz.uzinfocom.app.modules.act.application.exception.ActScopeViolationException;
import uz.uzinfocom.app.modules.act.application.query.mapper.ActMapper;
import uz.uzinfocom.app.modules.act.infrastructure.persistence.repository.ActRepository;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mirrors {@code CardQueryServiceScopedViewsTest}'s auth-gate coverage for
 * {@code findMine} — Act's "mine" view stays personal in every case (no
 * organization-scope broadening; that was only requested for cards).
 */
class ActQueryServiceTest {

    private ActRepository actRepository;
    private CurrentUserProvider currentUserProvider;
    private ActQueryService service;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        actRepository = mock(ActRepository.class);
        ActMapper actMapper = mock(ActMapper.class);
        currentUserProvider = mock(CurrentUserProvider.class);

        service = new ActQueryService(actRepository, actMapper, currentUserProvider);

        when(actRepository.findBy(any(Specification.class), any(Function.class)))
                .thenReturn(Page.empty());
    }

    @Test
    void findMineRunsForAnAuthenticatedUser() {
        when(currentUserProvider.userIdOrNull()).thenReturn(42L);

        service.findMine(emptyFilter());

        verify(actRepository, times(1)).findBy(any(Specification.class), any(Function.class));
    }

    @Test
    void findMineRefusesAnUnauthenticatedCaller() {
        when(currentUserProvider.userIdOrNull()).thenReturn(null);

        assertThatThrownBy(() -> service.findMine(emptyFilter()))
                .isInstanceOf(ActScopeViolationException.class);
    }

    private ActFilterRequest emptyFilter() {
        return new ActFilterRequest(1, 20, null, null, null, null, null, null);
    }
}
