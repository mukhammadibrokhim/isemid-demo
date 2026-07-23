package uz.uzinfocom.app.modules.act.application.handler.act224;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.act.application.handler.ActTypeHandler;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.Act224DetailResponse;
import uz.uzinfocom.app.modules.act.application.query.mapper.ActDetailMapper;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.domain.model.act224.Act224;
import uz.uzinfocom.app.modules.act.domain.model.act224.Act224Detail;
import uz.uzinfocom.app.modules.act.mapper.act224.Act224Mapper;
import uz.uzinfocom.app.modules.act.web.dto.request.Act224Request;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.persistence.sync.ChildCollectionSync;

@Component
@RequiredArgsConstructor
public class Act224Handler implements ActTypeHandler<Act224, Act224Request, Act224DetailResponse> {

    private final Act224Mapper mapper;
    private final ActDetailMapper actDetailMapper;
    private final AuditResolver auditResolver;

    @Override
    public ActType getType() {
        return ActType.ACT224;
    }

    @Override
    public Act224 createBlank() {
        return new Act224();
    }

    @Override
    public void update(Act224 act, Act224Request request) {
        mapper.copyOwnFields(act, request);

        if (act.getInstitution() != null) {
            act.getInstitution().normalize();
        }

        ChildCollectionSync.sync(
                act, act.getAct224Details(), request.recommendations(),
                mapper::toEntity, mapper::update, Act224Detail::setAct224
        );
    }

    @Override
    public void validate(Act224 act) {
        // No cross-field business rules beyond bean validation identified for this type.
    }

    @Override
    public Act224DetailResponse toResponse(Act224 act) {
        return (Act224DetailResponse) actDetailMapper.toDetailResponse(act, auditResolver.resolve(act));
    }
}
