package uz.uzinfocom.app.modules.act.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
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

    @Transactional(readOnly = true)
    public Page<ActTableResponse> findByCard(ActFilterRequest filter) {
        Pageable pageable = PageableUtils.of(filter, ActSortFields.ALLOWED);

        Page<ActTableProjection> page = Objects.requireNonNull(actRepository.findBy(
                ActSpecification.byFilter(filter),
                query ->
                        query.as(ActTableProjection.class)
                                .page(pageable)), "Act table page returned null"
        );

        return page.map(actMapper::toTableResponse);
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
        return actRepository.findById(id)
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
