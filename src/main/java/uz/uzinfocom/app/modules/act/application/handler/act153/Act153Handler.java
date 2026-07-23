package uz.uzinfocom.app.modules.act.application.handler.act153;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.act.application.handler.ActTypeHandler;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.Act153DetailResponse;
import uz.uzinfocom.app.modules.act.application.query.mapper.ActDetailMapper;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.domain.model.act153.Act153;
import uz.uzinfocom.app.modules.act.domain.model.act153.Act153Detail;
import uz.uzinfocom.app.modules.act.mapper.act153.Act153Mapper;
import uz.uzinfocom.app.modules.act.web.dto.request.Act153Request;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.persistence.sync.ChildCollectionSync;

@Component
@RequiredArgsConstructor
public class Act153Handler implements ActTypeHandler<Act153, Act153Request, Act153DetailResponse> {

    private final Act153Mapper mapper;
    private final ActDetailMapper actDetailMapper;
    private final AuditResolver auditResolver;

    @Override
    public ActType getType() {
        return ActType.ACT153;
    }

    @Override
    public Act153 createBlank() {
        return new Act153();
    }

    @Override
    public void update(Act153 act, Act153Request request) {
        mapper.copyOwnFields(act, request);

        if (act.getInstitution() != null) {
            act.getInstitution().normalize();
        }

        ChildCollectionSync.sync(
                act, act.getAct153Details(), request.samples(),
                mapper::toEntity, mapper::update, Act153Detail::setAct153
        );
    }

    @Override
    public void validate(Act153 act) {
        // No cross-field business rules beyond bean validation identified for this type.
    }

    @Override
    public Act153DetailResponse toResponse(Act153 act) {
        return (Act153DetailResponse) actDetailMapper.toDetailResponse(act, auditResolver.resolve(act));
    }
}
