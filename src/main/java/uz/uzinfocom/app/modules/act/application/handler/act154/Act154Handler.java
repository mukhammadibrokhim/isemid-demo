package uz.uzinfocom.app.modules.act.application.handler.act154;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.act.application.handler.ActTypeHandler;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.Act154DetailResponse;
import uz.uzinfocom.app.modules.act.application.query.mapper.ActDetailMapper;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.domain.model.act154.Act154;
import uz.uzinfocom.app.modules.act.domain.model.act154.Act154Detail;
import uz.uzinfocom.app.modules.act.mapper.act154.Act154Mapper;
import uz.uzinfocom.app.modules.act.web.dto.request.Act154Request;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.persistence.sync.ChildCollectionSync;

@Component
@RequiredArgsConstructor
public class Act154Handler implements ActTypeHandler<Act154, Act154Request, Act154DetailResponse> {

    private final Act154Mapper mapper;
    private final ActDetailMapper actDetailMapper;
    private final AuditResolver auditResolver;

    @Override
    public ActType getType() {
        return ActType.ACT154;
    }

    @Override
    public Act154 createBlank() {
        return new Act154();
    }

    @Override
    public void update(Act154 act, Act154Request request) {
        mapper.copyOwnFields(act, request);

        if (act.getInstitution() != null) {
            act.getInstitution().normalize();
        }

        ChildCollectionSync.sync(
                act, act.getAct154Details(), request.samples(),
                mapper::toEntity, mapper::update, Act154Detail::setAct154
        );
    }

    @Override
    public void validate(Act154 act) {
        // No cross-field business rules beyond bean validation identified for this type.
    }

    @Override
    public Act154DetailResponse toResponse(Act154 act) {
        return (Act154DetailResponse) actDetailMapper.toDetailResponse(act, auditResolver.resolve(act));
    }
}
