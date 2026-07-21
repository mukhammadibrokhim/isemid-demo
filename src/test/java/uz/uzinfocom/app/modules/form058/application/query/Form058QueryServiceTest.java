package uz.uzinfocom.app.modules.form058.application.query;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import uz.uzinfocom.app.modules.card.application.query.CardQueryService;
import uz.uzinfocom.app.modules.form058.application.query.mapper.Form058DetailResponseMapper;
import uz.uzinfocom.app.modules.form058.application.query.mapper.Form058PdfMapper;
import uz.uzinfocom.app.modules.form058.application.query.mapper.Form058TableMapper;
import uz.uzinfocom.app.modules.form058.domain.enums.FormStatus;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.specification.Form058Specification;
import uz.uzinfocom.app.modules.form058.web.dto.request.enums.Form058Direction;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.jpa.ExplainRowCountEstimator;
import uz.uzinfocom.app.platform.security.authorization.AdminAccessGuard;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class Form058QueryServiceTest {

    private final Form058QueryService service = new Form058QueryService(
            mock(Form058JpaRepository.class),
            mock(OrganizationScopeResolver.class),
            mock(Form058Specification.class),
            mock(Form058DetailResponseMapper.class),
            mock(Form058PdfMapper.class),
            mock(Form058TableMapper.class),
            mock(AdminAccessGuard.class),
            mock(AuditResolver.class),
            mock(CardQueryService.class),
            mock(ExplainRowCountEstimator.class)
    );

    @Test
    void defaultsToCreatedAtDescWithIdAsTiebreaker() {
        Pageable pageable = service.resolvePageable(filter(null, null));

        Iterator<Sort.Order> orders = pageable.getSort().iterator();
        Sort.Order first = orders.next();
        Sort.Order second = orders.next();

        assertThat(first.getProperty()).isEqualTo("createdAt");
        assertThat(first.getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(second.getProperty()).isEqualTo("id");
        assertThat(second.getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(orders.hasNext()).isFalse();
    }

    @Test
    void doesNotDuplicateIdTiebreakerWhenSortByIsAlreadyId() {
        Pageable pageable = service.resolvePageable(filter("id", "ASC"));

        Iterator<Sort.Order> orders = pageable.getSort().iterator();
        Sort.Order only = orders.next();

        assertThat(only.getProperty()).isEqualTo("id");
        assertThat(only.getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(orders.hasNext()).isFalse();
    }

    @Test
    void appendsIdTiebreakerWhenSortingByAnotherAllowedField() {
        Pageable pageable = service.resolvePageable(filter("status", "ASC"));

        Iterator<Sort.Order> orders = pageable.getSort().iterator();
        Sort.Order first = orders.next();
        Sort.Order second = orders.next();

        assertThat(first.getProperty()).isEqualTo("status");
        assertThat(first.getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(second.getProperty()).isEqualTo("id");
        assertThat(second.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    private Form058Filter filter(String sortBy, String sortDir) {
        return new Form058Filter(
                null, null, sortBy, sortDir,
                (FormStatus) null, Form058Direction.OUTGOING, null, null,
                null, null, null, null,
                null, null, null, null, null
        );
    }
}
