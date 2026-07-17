package uz.uzinfocom.app.modules.act.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.modules.act.application.exception.ActNotFoundException;
import uz.uzinfocom.app.modules.act.application.exception.ActScopeViolationException;
import uz.uzinfocom.app.modules.act.application.query.dto.ActDetailResponse;
import uz.uzinfocom.app.modules.act.application.query.dto.ActTableResponse;
import uz.uzinfocom.app.modules.act.application.query.mapper.ActMapper;
import uz.uzinfocom.app.modules.act.application.query.projection.ActTableProjection;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.act.infrastructure.persistence.repository.ActRepository;
import uz.uzinfocom.app.modules.act.infrastructure.persistence.specification.ActSpecification;
import uz.uzinfocom.app.platform.security.context.CurrentUserProvider;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ActQueryService {

    private final ActRepository actRepository;
    private final ActMapper actMapper;
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
        Act act = actRepository.findById(id)
                .orElseThrow(() -> new ActNotFoundException(id));

        return actMapper.toDetailResponse(act);
    }

    private Long requireCurrentUserId() {
        Long userId = currentUserProvider.userIdOrNull();
        if (userId == null) {
            throw new ActScopeViolationException();
        }
        return userId;
    }
}
