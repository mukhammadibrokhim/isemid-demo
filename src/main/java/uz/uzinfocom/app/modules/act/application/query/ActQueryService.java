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
import uz.uzinfocom.app.modules.iam.domain.User;
import uz.uzinfocom.app.platform.persistence.audit.AuditResolver;
import uz.uzinfocom.app.modules.iam.domain.Organization;
import uz.uzinfocom.app.orchestration.scope.OrganizationScopeResolver;
import uz.uzinfocom.app.orchestration.scope.ResolvedOrganizationScope;
import uz.uzinfocom.app.orchestration.scope.jpa.SenderReceiverScopePredicateFactory;
import uz.uzinfocom.app.platform.security.context.CurrentOrganizationContext;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

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

    /**
     * A card's acts, behind {@code GET /v1/cards/{id}/acts} — limited to the
     * caller's organization scope the same way {@link #findAll} is, so a card
     * id from outside it yields an empty page.
     */
    @Transactional(readOnly = true)
    public Page<ActTableResponse> findByCard(ActFilterRequest filter) {
        ResolvedOrganizationScope scope = currentScope();
        return queryTable(ActSpecification.byFilter(filter).and(inScope(scope)), filter);
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

        return queryTable(ActSpecification.byFilter(filter).and(inScope(scope)), filter);
    }

    private Specification<Act> inScope(ResolvedOrganizationScope scope) {
        return (root, query, cb) -> CardCaseScopeSpecification.scopePredicate(
                root.join("card"), cb, scopePredicateFactory, scope, CaseFormType.ANY);
    }

    /**
     * Single-act visibility: within the caller's organization scope (as in
     * {@link #findAll}), or attached to the caller — an attached employee
     * always reaches the acts {@link #findMine} lists. Checked through a
     * subquery so the users join can't duplicate the row.
     */
    private Specification<Act> visibleById(Long id) {
        ResolvedOrganizationScope scope = currentScope();
        Long userId = currentUserProvider.userIdOrNull();
        return (root, query, cb) -> {
            Subquery<Long> attached = query.subquery(Long.class);
            Root<Act> attachedRoot = attached.from(Act.class);
            Join<Act, User> users = attachedRoot.join("users");
            attached.select(attachedRoot.get("id")).where(
                    cb.equal(attachedRoot.get("id"), root.get("id")),
                    cb.equal(users.get("id"), userId)
            );
            return cb.and(
                    cb.equal(root.get("id"), id),
                    cb.isFalse(root.get("deleteInfo").get("deleted")),
                    cb.or(
                            CardCaseScopeSpecification.scopePredicate(
                                    root.join("card"), cb, scopePredicateFactory, scope, CaseFormType.ANY),
                            userId == null ? cb.disjunction() : cb.exists(attached)
                    )
            );
        };
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
        ActFilterRequest mine = filter.scopedToAttachedUser(requireCurrentUserId());
        return queryTable(ActSpecification.byFilter(mine), mine);
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

    /** Outside the caller's scope reads as not found (404), like {@code CardQueryService}. */
    private Act findAct(Long id) {
        return actRepository.findOne(visibleById(id))
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
