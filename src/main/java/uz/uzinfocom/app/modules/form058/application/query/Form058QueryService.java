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
import uz.uzinfocom.app.modules.form058.application.query.dto.Form058AffiliatedTableResponse;
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
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;
import uz.uzinfocom.app.modules.patient.infrastructure.persistence.repository.PatientAffiliationJpaRepository;
import uz.uzinfocom.app.modules.patient.infrastructure.persistence.repository.PatientAffiliationOrganizationType;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.iam.domain.Organization;
import uz.uzinfocom.app.platform.scope.FormAccessScopeResolver;
import uz.uzinfocom.app.platform.scope.OrganizationScopeMode;
import uz.uzinfocom.app.platform.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.platform.scope.jpa.ExplainRowCountEstimator;
import uz.uzinfocom.app.platform.security.authorization.AdminAccessGuard;
import uz.uzinfocom.app.platform.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.shared.pagination.PageableRequest;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final PatientAffiliationJpaRepository patientAffiliationRepository;

    /**
     * Same direction-based scope switch as {@link #findAll} (including the super-admin
     * guard for {@code ALL}), exposed for callers that only need the {@code Specification}
     * itself - e.g. {@code Form058ExcelExportSource}, which streams every matching row
     * rather than a single page of them.
     */
    public Specification<Form058> resolveSpecification(Form058Filter filter) {
        ResolvedOrganizationScope scope = currentScope();

        return switch (filter.direction()) {
            case OUTGOING -> form058Specification.table(filter, scope, false);
            case INCOMING -> form058Specification.table(filter, scope, true);
            case ALL -> {
                form058AccessGuard.requireSuperAdmin();
                yield form058Specification.tableUnscoped(filter, scope);
            }
        };
    }

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

        return assemblePage(spec, pageable, filter.direction(), canEstimateTotal);
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

        return assemblePage(spec, pageable, filter.direction(), filter.hasNoAdditionalFilters());
    }

    /**
     * {@code GET /v1/form-058/affiliated} - forms visible to the current
     * organization solely because the patient's workplace or place of study
     * is that organization, independent of sender/receiver. Kept as its own
     * method/endpoint rather than a mode-switch on {@link #findAll} - see
     * {@link Form058AffiliatedFilter}'s javadoc for why.
     * <p>
     * Each row is additionally labelled with which affiliation type
     * (WORKPLACE/EDUCATIONAL) explains its visibility - resolved in one bulk
     * query over the page's patient ids rather than one lookup per row.
     */
    public Page<Form058AffiliatedTableResponse> findAllAffiliated(Form058AffiliatedFilter filter) {
        ResolvedOrganizationScope scope = currentScope();

        Pageable pageable = resolvePageable(filter);
        Specification<Form058> spec = form058Specification.affiliatedTable(filter, scope.organizationId());

        // Never estimate here: unlike findByScope/findAllUnscoped, this spec is always
        // restricted by patientAffiliationExists(organizationId), so the unfiltered-table
        // planner estimate (explainActiveRowCountPlan) would report the whole active
        // form058 table's row count instead of this organization's affiliated total.
        Page<Form058TableResponse> page = assemblePage(spec, pageable, Form058Direction.ALL, false);
        return page.map(withAffiliationType(page.getContent(), scope.organizationId()));
    }

    /**
     * Builds the patientId -> AffiliationType lookup for one page of results
     * up front, then returns a per-row mapping function - avoids an N+1
     * query (one affiliation lookup per row) for what would otherwise be a
     * per-row {@code existsBy...} call.
     */
    private Function<Form058TableResponse, Form058AffiliatedTableResponse> withAffiliationType(
            List<Form058TableResponse> content,
            Long organizationId
    ) {
        List<Long> patientIds = content.stream()
                .map(row -> row.patient() != null ? row.patient().id() : null)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, AffiliationType> typeByPatientId = patientIds.isEmpty()
                ? Map.of()
                : patientAffiliationRepository
                        .findTypesByPatientIdInAndOrganizationIdAndTypeIn(patientIds, organizationId, FormAccessScopeResolver.AFFILIATION_TYPES)
                        .stream()
                        .collect(Collectors.toMap(
                                PatientAffiliationOrganizationType::patientId,
                                PatientAffiliationOrganizationType::type,
                                (first, second) -> first
                        ));

        return row -> new Form058AffiliatedTableResponse(
                row,
                row.patient() != null ? typeByPatientId.get(row.patient().id()) : null
        );
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
            Form058Direction direction,
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
                .map(projection -> form058TableMapper.toTableResponse(projection, direction))
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
    public Pageable resolvePageable(PageableRequest filter) {
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
                1, 200, null, null, form058Id, null, null, null, null, null
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