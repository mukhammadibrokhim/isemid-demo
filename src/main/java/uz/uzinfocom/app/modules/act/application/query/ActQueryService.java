package uz.uzinfocom.app.modules.act.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.act.application.exception.ActNotFoundException;
import uz.uzinfocom.app.modules.act.application.exception.ActScopeViolationException;
import uz.uzinfocom.app.modules.act.application.query.dto.ActTableResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.ActDetailResponse;
import uz.uzinfocom.app.modules.act.application.query.mapper.ActDetailMapper;
import uz.uzinfocom.app.modules.act.application.query.mapper.ActMapper;
import uz.uzinfocom.app.modules.act.application.query.projection.ActTableProjection;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.act.infrastructure.persistence.repository.ActRepository;
import uz.uzinfocom.app.modules.act.infrastructure.persistence.specification.ActSpecification;
import uz.uzinfocom.app.modules.card.domain.enums.CaseFormType;
import uz.uzinfocom.app.modules.card.infrastructure.persistence.specification.CardCaseScopeSpecification;
import uz.uzinfocom.app.platform.persistence.audit.AuditResolver;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.orchestration.scope.jpa.SenderReceiverScopePredicateFactory;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ActQueryService {

    private final ActRepository actRepository;
    private final ActMapper actMapper;
    private final ActDetailMapper actDetailMapper;
    private final AuditResolver auditResolver;
    private final CurrentUserProvider currentUserProvider;
    private final OrganizationScopeResolver organizationScopeResolver;
    private final SenderReceiverScopePredicateFactory scopePredicateFactory;

    @Transactional(readOnly = true)
    public Page<ActTableResponse> findByCard(ActFilterRequest filter) {
        return queryTable(ActSpecification.byFilter(filter), filter);
    }

    /**
     * Organization-scoped listing behind {@code GET /v1/acts} — every act
     * visible within the caller's organization scope, resolved one hop
     * further than {@link uz.uzinfocom.app.modules.card.application.query.CardQueryService#findAll}
     * (act -> card -> form058/form0581, see {@link CardCaseScopeSpecification}).
     * Unlike {@link #findMine}, this never narrows to the attached employee.
     */
    @Transactional(readOnly = true)
    public Page<ActTableResponse> findAll(ActFilterRequest filter) {
        ResolvedOrganizationScope scope = currentScope();

        Specification<Act> spec = ActSpecification.byFilter(filter)
                .and((root, query, cb) -> CardCaseScopeSpecification.scopePredicate(
                        root.get("card"), cb, scopePredicateFactory, scope, CaseFormType.ANY));

        return queryTable(spec, filter);
    }

    private Page<ActTableResponse> queryTable(Specification<Act> spec, ActFilterRequest filter) {
        Pageable pageable = PageableUtils.of(filter, ActSortFields.ALLOWED);

        Page<ActTableProjection> page = Objects.requireNonNull(actRepository.findBy(
                spec,
                query ->
                        query.as(ActTableProjection.class)
                                .page(pageable)), "Act table page returned null"
        );

        return page.map(actMapper::toTableResponse);
    }

    private ResolvedOrganizationScope currentScope() {
        Organization organization = CurrentOrganizationContext.getOptional()
                .orElseThrow(ActScopeViolationException::new);
        return organizationScopeResolver.resolve(organization);
    }

    /**
     * The attached employee's own view — {@code assignedToUserId} is always
     * forced to the authenticated user, mirroring {@code CardQueryService}'s
     * personal branch. Unlike Card's {@code findMine}, this does not widen
     * for broader-scope organizations — that behavior was only requested for
     * cards.
     */
    @Transactional(readOnly = true)
    public Page<ActTableResponse> findMine(ActFilterRequest filter) {
        return findByCard(filter.scopedToAttachedUser(requireCurrentUserId()));
    }

    @Transactional(readOnly = true)
    public ActDetailResponse getById(Long id) {
        Act act = findAct(id);
        return actDetailMapper.toDetailResponse(act, auditResolver.resolve(act));
    }

    /**
     * Same content as {@link #getById}, minus {@code audit} — Act's
     * embeddables already carry human-readable uz/ru names alongside their
     * codes, so there is no separate print-oriented shape to build (see
     * {@link ActDetailResponse}'s javadoc); the print view just has no use
     * for who/when created or last updated the record. Kept as its own
     * method/route for a stable, clearly-named frontend contract.
     */
    @Transactional(readOnly = true)
    public ActDetailResponse getPdf(Long id) {
        return actDetailMapper.toDetailResponse(findAct(id), null);
    }

    private Act findAct(Long id) {
        return actRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ActNotFoundException(id));
    }

    private Long requireCurrentUserId() {
        Long userId = currentUserProvider.userIdOrNull();
        if (userId == null) {
            throw new ActScopeViolationException();
        }
        return userId;
    }
}
