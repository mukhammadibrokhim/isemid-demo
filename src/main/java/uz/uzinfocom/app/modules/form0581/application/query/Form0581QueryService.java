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
import uz.uzinfocom.app.modules.form0581.application.query.dto.Form0581AffiliatedTableResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.Form0581TableResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.detail.Form0581DetailResponse;
import uz.uzinfocom.app.modules.form0581.application.query.dto.pdf.Form0581PdfResponse;
import uz.uzinfocom.app.modules.form0581.application.query.mapper.Form0581DetailResponseMapper;
import uz.uzinfocom.app.modules.form0581.application.query.mapper.Form0581PdfMapper;
import uz.uzinfocom.app.modules.form0581.application.query.mapper.Form0581TableMapper;
import uz.uzinfocom.app.modules.form0581.application.query.projection.Form0581TableProjection;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.repository.Form0581JpaRepository;
import uz.uzinfocom.app.modules.form0581.infrastructure.persistence.specification.Form0581Specification;
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
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class Form0581QueryService {

    private final Form0581JpaRepository repository;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final Form0581Specification form0581Specification;
    private final Form0581DetailResponseMapper form0581DetailResponseMapper;
    private final Form0581PdfMapper form0581PdfMapper;
    private final Form0581TableMapper form0581TableMapper;
    private final AdminAccessGuard form0581AccessGuard;
    private final AuditResolver auditResolver;
    private final ExplainRowCountEstimator explainRowCountEstimator;
    private final PatientAffiliationJpaRepository patientAffiliationRepository;

    /**
     * Same direction-based scope switch as {@link #findAll} (including the super-admin
     * guard for {@code ALL}), exposed for callers that only need the {@code Specification}
     * itself - e.g. {@code Form0581ExcelExportSource}, which streams every matching row
     * rather than a single page of them.
     */
    public Specification<Form0581> resolveSpecification(Form0581Filter filter) {
        ResolvedOrganizationScope scope = currentScope();

        return switch (filter.direction()) {
            case OUTGOING -> form0581Specification.table(filter, scope, false);
            case INCOMING -> form0581Specification.table(filter, scope, true);
            case ALL -> {
                form0581AccessGuard.requireSuperAdmin();
                yield form0581Specification.tableUnscoped(filter);
            }
        };
    }

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

        return assemblePage(spec, pageable, filter.direction(), canEstimateTotal);
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

        return assemblePage(spec, pageable, filter.direction(), filter.hasNoAdditionalFilters());
    }

    /**
     * {@code GET /v1/form-058-1/affiliated} - forms visible to the current
     * organization solely because the patient's workplace or place of study
     * is that organization, independent of sender/receiver. Kept as its own
     * method/endpoint rather than a mode-switch on {@link #findAll} - see
     * {@link Form0581AffiliatedFilter}'s javadoc for why. Mirrors {@code
     * Form058QueryService#findAllAffiliated}.
     * <p>
     * Each row is additionally labelled with which affiliation type
     * (WORKPLACE/EDUCATIONAL) explains its visibility - resolved in one bulk
     * query over the page's patient ids rather than one lookup per row.
     */
    public Page<Form0581AffiliatedTableResponse> findAllAffiliated(Form0581AffiliatedFilter filter) {
        ResolvedOrganizationScope scope = currentScope();

        Pageable pageable = PageableUtils.of(filter, Form0581SortFields.ALLOWED);
        Specification<Form0581> spec = form0581Specification.affiliatedTable(filter, scope.organizationId());

        // Never estimate here: unlike findByScope/findAllUnscoped, this spec is always
        // restricted by patientAffiliationExists(organizationId), so the unfiltered-table
        // planner estimate (explainActiveRowCountPlan) would report the whole active
        // form0581 table's row count instead of this organization's affiliated total.
        Page<Form0581TableResponse> page = assemblePage(spec, pageable, Form0581Direction.ALL, false);
        return page.map(withAffiliationType(page.getContent(), scope.organizationId()));
    }

    /**
     * Builds the patientId -> AffiliationType lookup for one page of results
     * up front, then returns a per-row mapping function - avoids an N+1
     * query (one affiliation lookup per row) for what would otherwise be a
     * per-row {@code existsBy...} call.
     */
    private Function<Form0581TableResponse, Form0581AffiliatedTableResponse> withAffiliationType(
            List<Form0581TableResponse> content,
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

        return row -> new Form0581AffiliatedTableResponse(
                row,
                row.patient() != null ? typeByPatientId.get(row.patient().id()) : null
        );
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
            Form0581Direction direction,
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
                .map(projection -> form0581TableMapper.toTableResponse(projection, direction))
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

    public Form0581PdfResponse getPdf(Long id) {
        ResolvedOrganizationScope scope = currentScope();

        Form0581 form0581 = repository
                .findOne(form0581Specification.visibleById(id, scope))
                .orElseThrow(() -> new Form0581NotFoundException(id));

        return form0581PdfMapper.toPdfResponse(form0581);
    }

    private ResolvedOrganizationScope currentScope() {
        return organizationScopeResolver.resolve(currentOrganization());
    }

    private Organization currentOrganization() {
        return CurrentOrganizationContext.getOptional()
                .orElseThrow(Form0581ScopeViolationException::new);
    }
}
