package uz.uzinfocom.app.platform.iam.application.action.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.uzinfocom.app.platform.iam.application.action.query.dto.ActionDetailResponse;
import uz.uzinfocom.app.platform.iam.application.action.query.dto.ActionFilterRequest;
import uz.uzinfocom.app.platform.iam.application.action.query.dto.ActionTableResponse;
import uz.uzinfocom.app.platform.iam.application.action.query.mapper.ActionQueryMapper;
import uz.uzinfocom.app.platform.iam.application.action.query.projection.ActionTableProjection;
import uz.uzinfocom.app.platform.iam.application.action.query.specification.ActionSpecification;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.iam.domain.Action;
import uz.uzinfocom.app.platform.iam.repository.ActionRepository;
import uz.uzinfocom.app.shared.exception.NotFoundException;
import uz.uzinfocom.app.shared.pagination.PageableUtils;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ActionQueryService {

    private final ActionRepository actionRepository;
    private final ActionQueryMapper actionQueryMapper;
    private final AuditResolver auditResolver;

    @Transactional(readOnly = true)
    public Page<ActionTableResponse> findTable(ActionFilterRequest request) {
        Pageable pageable = PageableUtils.of(
                request,
                ActionSortFields.ALLOWED
        );

        Page<ActionTableProjection> page = Objects.requireNonNull(actionRepository.findBy(
                ActionSpecification.byFilter(request),
                query -> query
                        .as(ActionTableProjection.class)
                        .page(pageable)
                ), "Action table page return null"
        );

        return page.map(actionQueryMapper::toTableResponse);
    }

    @Transactional(readOnly = true)
    public ActionDetailResponse getById(Long id) {
        Action action = actionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("action.not_found_by_id", id));
        return actionQueryMapper.toDetailResponse(action, auditResolver.resolve(action));
    }
}
