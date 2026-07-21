package uz.uzinfocom.app.modules.form058.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.card.application.query.CardFilterRequest;
import uz.uzinfocom.app.modules.card.application.query.CardQueryService;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTableResponse;
import uz.uzinfocom.app.modules.form058.application.exception.Form058NotFoundException;
import uz.uzinfocom.app.modules.form058.application.exception.Form058ScopeViolationException;
import uz.uzinfocom.app.modules.form058.application.query.dto.Form058TableResponse;
import uz.uzinfocom.app.modules.form058.application.query.dto.detail.Form058DetailResponse;
import uz.uzinfocom.app.modules.form058.application.query.dto.pdf.Form058PdfResponse;
import uz.uzinfocom.app.modules.form058.application.query.mapper.Form058DetailResponseMapper;
import uz.uzinfocom.app.modules.form058.application.query.mapper.Form058PdfMapper;
import uz.uzinfocom.app.modules.form058.application.query.mapper.Form058TableMapper;
import uz.uzinfocom.app.modules.form058.application.query.projection.Form058TableProjection;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.repository.Form058JpaRepository;
import uz.uzinfocom.app.modules.form058.infrastructure.persistence.specification.Form058Specification;
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
public class Form058QueryService {

    private final Form058JpaRepository repository;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final Form058Specification form058Specification;
    private final Form058DetailResponseMapper form058DetailResponseMapper;
    private final Form058PdfMapper form058PdfMapper;
    private final Form058TableMapper form058TableMapper;
    private final AdminAccessGuard form058AccessGuard;
    private final AuditResolver auditResolver;
    private final CardQueryService cardQueryService;
    private final ExplainRowCountEstimator explainRowCountEstimator;

    public Page<Form058TableResponse> findAll(Form058Filter filter) {
        ResolvedOrganizationScope scope = currentScope();

        return switch (filter.direction()) {
            case OUTGOING -> findByScope(filter, scope, false);
            case INCOMING -> findByScope(filter, scope, true);
            case ALL -> findAllUnscoped(filter, scope);
        };
    }

    protected Page<Form058TableResponse> findByScope(
            Form058Filter filter,
            ResolvedOrganizationScope scope,
            Boolean received
    ) {
        Pageable pageable = resolvePageable(filter);
        Specification<Form058> spec = form058Specification.table(filter, scope, received);

        boolean canEstimateTotal = scope.mode() == OrganizationScopeMode.ALL
                && filter.hasNoAdditionalFilters();

        return assemblePage(spec, pageable, filter, canEstimateTotal);
    }

    /**
     * ALL is a super-admin-only view across every organization: no sender/receiver
     * scope restriction is applied. requireSuperAdmin() is the only gate protecting
     * this from being a full data leak, so it must never be removed.
     */
    private Page<Form058TableResponse> findAllUnscoped(
            Form058Filter filter,
            ResolvedOrganizationScope scope
    ) {
        form058AccessGuard.requireSuperAdmin();

        Pageable pageable = resolvePageable(filter);
        Specification<Form058> spec = form058Specification.tableUnscoped(filter, scope);

        return assemblePage(spec, pageable, filter, filter.hasNoAdditionalFilters());
    }

    /**
     * Fetches the page content via a count-free {@code slice()} and resolves the
     * pagination total separately - either the exact {@code COUNT(*)} (fast already for
     * any real predicate, since it hits one of the composite sender/receiver indexes), or,
     * when the caller confirms the predicate is effectively unfiltered (broad SANEPID
     * scope, no additional filter fields), a fast planner row estimate instead. An exact
     * COUNT(*) over an unfiltered 600k+ row table costs tens of milliseconds on its own
     * (confirmed via EXPLAIN ANALYZE) for no benefit: nobody reads "exactly 600,010" as
     * meaningfully different from "about 600,000" in a paginated list's total.
     */
    private Page<Form058TableResponse> assemblePage(
            Specification<Form058> spec,
            Pageable pageable,
            Form058Filter filter,
            boolean canEstimateTotal
    ) {
        Slice<Form058TableProjection> slice = Objects.requireNonNull(
                repository.findBy(
                        spec,
                        query -> query
                                .as(Form058TableProjection.class)
                                .sortBy(pageable.getSort())
                                .slice(pageable)
                ),
                "Form058 table slice returned null"
        );

        long total = canEstimateTotal
                ? explainRowCountEstimator.estimate(
                        repository.explainActiveRowCountPlan(),
                        () -> repository.count(spec))
                : repository.count(spec);

        List<Form058TableResponse> content = slice.getContent().stream()
                .map(projection -> form058TableMapper.toTableResponse(projection, filter.direction()))
                .toList();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Defaults to sorting by createdAt rather than id. For a SANEPID org whose
     * scope resolves to ALL (e.g. REPUBLICAN level), the sender/receiver
     * predicate is a no-op (see OrganizationScopePredicateFactory) and this
     * query's only real filter is often the date range — with the default
     * "id DESC" sort, Postgres favors scanning form058_pkey backwards to
     * satisfy the ORDER BY, then discards non-matching rows one at a time.
     * On this table's real data that plan scans the bulk of 600k+ rows
     * (confirmed via EXPLAIN ANALYZE: 400-800ms) instead of using the
     * existing idx_form058_created_at index (confirmed: under 1ms). Sorting
     * by createdAt lets Postgres use that index for both the filter and the
     * order; id is appended as a stable tiebreaker for rows sharing a
     * timestamp.
     */
    Pageable resolvePageable(Form058Filter filter) {
        Pageable pageable = PageableUtils.of(
                filter,
                "createdAt",
                Sort.Direction.DESC,
                Form058SortFields.ALLOWED
        );

        Sort sort = pageable.getSort();
        boolean alreadySortsById = sort.stream()
                .anyMatch(order -> order.getProperty().equals("id"));

        if (!alreadySortsById) {
            Sort.Order primary = sort.iterator().next();
            sort = sort.and(Sort.by(primary.getDirection(), "id"));
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    public Form058DetailResponse getById(Long id) {
        ResolvedOrganizationScope scope = currentScope();

        Form058 form058 = repository
                .findOne(form058Specification.visibleById(id, scope))
                .orElseThrow(() -> new Form058NotFoundException(id));

        return form058DetailResponseMapper.toDetailedResponse(
                form058,
                auditResolver.resolve(form058),
                linkedCards(form058.getId())
        );
    }

    public Form058DetailResponse getByNnuzb(String nnuzb) {
        ResolvedOrganizationScope scope = currentScope();

        Form058 form058 = repository
                .findOne(form058Specification.visibleByNnuzb(nnuzb, scope))
                .orElseThrow(() -> new Form058NotFoundException(nnuzb));

        return form058DetailResponseMapper.toDetailedResponse(
                form058,
                auditResolver.resolve(form058),
                linkedCards(form058.getId())
        );
    }

    public Form058PdfResponse getPdf(Long id) {
        ResolvedOrganizationScope scope = currentScope();

        Form058 form058 = repository
                .findOne(form058Specification.visibleById(id, scope))
                .orElseThrow(() -> new Form058NotFoundException(id));

        return form058PdfMapper.toPdfResponse(form058, linkedCards(form058.getId()));
    }

    private List<CardTableResponse> linkedCards(Long form058Id) {
        CardFilterRequest filter = new CardFilterRequest(
                1, 200, null, null, form058Id, null, null, null, null
        );
        return cardQueryService.findTable(filter).getContent();
    }

    private ResolvedOrganizationScope currentScope() {
        return organizationScopeResolver.resolve(currentOrganization());
    }

    private Organization currentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(Form058ScopeViolationException::new);
    }
}