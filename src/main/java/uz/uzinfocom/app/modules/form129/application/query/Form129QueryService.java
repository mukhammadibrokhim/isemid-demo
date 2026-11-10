package uz.uzinfocom.app.modules.form129.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.form129.application.exception.Form129NotFoundException;
import uz.uzinfocom.app.modules.form129.application.exception.Form129ScopeViolationException;
import uz.uzinfocom.app.modules.form129.application.query.dto.Form129TableResponse;
import uz.uzinfocom.app.modules.form129.application.query.dto.detail.Form129DetailResponse;
import uz.uzinfocom.app.modules.form129.application.query.mapper.Form129DetailResponseMapper;
import uz.uzinfocom.app.modules.form129.application.query.mapper.Form129TableMapper;
import uz.uzinfocom.app.modules.form129.application.query.projection.Form129TableProjection;
import uz.uzinfocom.app.modules.form129.domain.model.Form129;
import uz.uzinfocom.app.modules.form129.infrastructure.persistence.repository.Form129JpaRepository;
import uz.uzinfocom.app.modules.form129.infrastructure.persistence.specification.Form129Specification;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.persistence.audit.AuditResolver;
import uz.uzinfocom.app.platform.security.auth.AdminAccessGuard;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class Form129QueryService {

    private final Form129JpaRepository repository;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final Form129Specification form129Specification;
    private final Form129DetailResponseMapper form129DetailResponseMapper;
    private final Form129TableMapper form129TableMapper;
    private final AdminAccessGuard form129AccessGuard;
    private final AuditResolver auditResolver;

    public Page<Form129TableResponse> findAll(Form129Filter filter) {
        ResolvedOrganizationScope scope = currentScope();

        return switch (filter.direction()) {
            case OUTGOING -> findByScope(filter, scope, false);
            case INCOMING -> findByScope(filter, scope, true);
            case ALL -> findAllUnscoped(filter);
        };
    }

    private Page<Form129TableResponse> findByScope(
            Form129Filter filter,
            ResolvedOrganizationScope scope,
            Boolean received
    ) {
        Pageable pageable = PageableUtils.of(filter, Form129SortFields.ALLOWED);
        Specification<Form129> spec = form129Specification.table(filter, scope, received);

        return assemblePage(spec, pageable);
    }

    /**
     * ALL is a super-admin-only view across every organization: no
     * sender/receiver scope restriction is applied. requireSuperAdmin() is
     * the only gate protecting this from being a full data leak.
     */
    private Page<Form129TableResponse> findAllUnscoped(Form129Filter filter) {
        form129AccessGuard.requireSuperAdmin();

        Pageable pageable = PageableUtils.of(filter, Form129SortFields.ALLOWED);
        Specification<Form129> spec = form129Specification.tableUnscoped(filter);

        return assemblePage(spec, pageable);
    }

    private Page<Form129TableResponse> assemblePage(Specification<Form129> spec, Pageable pageable) {
        Slice<Form129TableProjection> slice = Objects.requireNonNull(
                repository.findBy(
                        spec,
                        query -> query
                                .as(Form129TableProjection.class)
                                .sortBy(pageable.getSort())
                                .slice(pageable)
                ),
                "Form129 table slice returned null"
        );

        long total = repository.count(spec);

        List<Form129TableResponse> content = slice.getContent().stream()
                .map(form129TableMapper::toTableResponse)
                .toList();

        return new PageImpl<>(content, pageable, total);
    }

    public Form129DetailResponse getById(Long id) {
        ResolvedOrganizationScope scope = currentScope();

        Form129 form129 = repository
                .findOne(form129Specification.visibleById(id, scope))
                .orElseThrow(() -> new Form129NotFoundException(id));

        return form129DetailResponseMapper.toDetailedResponse(form129, auditResolver.resolve(form129));
    }

    private ResolvedOrganizationScope currentScope() {
        return organizationScopeResolver.resolve(currentOrganization());
    }

    private Organization currentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(Form129ScopeViolationException::new);
    }
}
