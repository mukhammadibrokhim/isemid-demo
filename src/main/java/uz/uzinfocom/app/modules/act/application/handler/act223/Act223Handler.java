package uz.uzinfocom.app.modules.act.application.handler.act223;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.act.application.handler.ActTypeHandler;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.Act223DetailResponse;
import uz.uzinfocom.app.modules.act.application.query.mapper.ActDetailMapper;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.domain.model.act223.Act223;
import uz.uzinfocom.app.modules.act.domain.model.act223.Act223Detail;
import uz.uzinfocom.app.modules.act.mapper.act223.Act223Mapper;
import uz.uzinfocom.app.modules.act.web.dto.request.Act223Request;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.persistence.sync.ChildCollectionSync;

@Component
@RequiredArgsConstructor
public class Act223Handler implements ActTypeHandler<Act223, Act223Request, Act223DetailResponse> {

    private final Act223Mapper mapper;
    private final ActDetailMapper actDetailMapper;
    private final AuditResolver auditResolver;

    @Override
    public ActType getType() {
        return ActType.ACT223;
    }

    @Override
    public Act223 createBlank() {
        return new Act223();
    }

    @Override
    public void update(Act223 act, Act223Request request) {
        mapper.copyOwnFields(act, request);

        if (act.getInstitution() != null) {
            act.getInstitution().normalize();
        }

        ChildCollectionSync.sync(
                act, act.getAct223Details(), request.samples(),
                mapper::toEntity, mapper::update, Act223Detail::setAct223
        );
    }

    @Override
    public void validate(Act223 act) {
        // No cross-field business rules beyond bean validation identified for this type.
    }

    @Override
    public Act223DetailResponse toResponse(Act223 act) {
        return (Act223DetailResponse) actDetailMapper.toDetailResponse(act, auditResolver.resolve(act));
    }
}
