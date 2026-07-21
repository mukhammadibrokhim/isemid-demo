package uz.uzinfocom.app.modules.form0581.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581NotFoundException;
import uz.uzinfocom.app.modules.form0581.application.exception.Form0581ScopeViolationException;
import uz.uzinfocom.app.modules.form0581.application.query.dto.Form0581TableResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.detail.Form0581DetailResponse;
import uz.uzinfocom.app.modules.form0581.application.query.mapper.Form0581DetailResponseMapper;
import uz.uzinfocom.app.modules.form0581.application.query.mapper.Form0581TableMapper;
import uz.uzinfocom.app.modules.form0581.application.query.projection.Form0581TableProjection;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.specification.Form0581Specification;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.jpa.ExplainRowCountEstimator;
import uz.uzinfocom.app.platform.security.authorization.AdminAccessGuard;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class Form0581QueryService {

    private final Form0581JpaRepository repository;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final Form0581Specification form0581Specification;
    private final Form0581DetailResponseMapper form0581DetailResponseMapper;
    private final Form0581TableMapper form0581TableMapper;
    private final AdminAccessGuard form0581AccessGuard;
    private final AuditResolver auditResolver;
    private final ExplainRowCountEstimator explainRowCountEstimator;

    public Page<Form0581TableResponse> findAll(Form0581Filter filter) {
        ResolvedOrganizationScope scope = currentScope();

        return switch (filter.direction()) {
            case OUTGOING -> findByScope(filter, scope, false);
            case INCOMING -> findByScope(filter, scope, true);
            case ALL -> findAllUnscoped(filter);
        };
    }

    protected Page<Form0581TableResponse> findByScope(
            Form0581Filter filter,
            ResolvedOrganizationScope scope,
            Boolean received
    ) {
        Pageable pageable = PageableUtils.of(
                filter,
                Form0581SortFields.ALLOWED
        );
        Specification<Form0581> spec = form0581Specification.table(filter, scope, received);

        boolean canEstimateTotal = scope.mode() == OrganizationScopeMode.ALL
                && filter.hasNoAdditionalFilters();

        return assemblePage(spec, pageable, filter, canEstimateTotal);
    }

    /**
     * ALL is a super-admin-only view across every organization: no sender/receiver
     * scope restriction is applied. requireSuperAdmin() is the only gate protecting
     * this from being a full data leak, so it must never be removed.
     */
    private Page<Form0581TableResponse> findAllUnscoped(Form0581Filter filter) {
        form0581AccessGuard.requireSuperAdmin();

        Pageable pageable = PageableUtils.of(
                filter,
                Form0581SortFields.ALLOWED
        );
        Specification<Form0581> spec = form0581Specification.tableUnscoped(filter);

        return assemblePage(spec, pageable, filter, filter.hasNoAdditionalFilters());
    }

    /**
     * Fetches the page content via a count-free {@code slice()} and resolves the
     * pagination total separately - either the exact {@code COUNT(*)} (fast already for
     * any real predicate, since it hits one of the composite sender/receiver indexes), or,
     * when the caller confirms the predicate is effectively unfiltered (broad SANEPID
     * scope, no additional filter fields), a fast planner row estimate instead. See
     * Form058QueryService.assemblePage for the equivalent form058 rationale.
     */
    private Page<Form0581TableResponse> assemblePage(
            Specification<Form0581> spec,
            Pageable pageable,
            Form0581Filter filter,
            boolean canEstimateTotal
    ) {
        Slice<Form0581TableProjection> slice = Objects.requireNonNull(
                repository.findBy(
                        spec,
                        query -> query
                                .as(Form0581TableProjection.class)
                                .sortBy(pageable.getSort())
                                .slice(pageable)
                ),
                "Form0581 table slice returned null"
        );

        long total = canEstimateTotal
                ? explainRowCountEstimator.estimate(
                        repository.explainActiveRowCountPlan(),
                        () -> repository.count(spec))
                : repository.count(spec);

        List<Form0581TableResponse> content = slice.getContent().stream()
                .map(projection -> form0581TableMapper.toTableResponse(projection, filter.direction()))
                .toList();

        return new PageImpl<>(content, pageable, total);
    }

    public Form0581DetailResponse getById(Long id) {
        ResolvedOrganizationScope scope = currentScope();

        Form0581 form0581 = repository
                .findOne(form0581Specification.visibleById(id, scope))
                .orElseThrow(() -> new Form0581NotFoundException(id));

        return form0581DetailResponseMapper.toDetailedResponse(
                form0581,
                auditResolver.resolve(form0581)
        );
    }

    public Form0581DetailResponse getByDocumentValue(String documentValue) {
        ResolvedOrganizationScope scope = currentScope();

        Form0581 form0581 = repository
                .findOne(form0581Specification.visibleByDocumentValue(documentValue, scope))
                .orElseThrow(() -> new Form0581NotFoundException(documentValue));

        return form0581DetailResponseMapper.toDetailedResponse(
                form0581,
                auditResolver.resolve(form0581)
        );
    }

    private ResolvedOrganizationScope currentScope() {
        return organizationScopeResolver.resolve(currentOrganization());
    }

    private Organization currentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(Form0581ScopeViolationException::new);
    }
}
