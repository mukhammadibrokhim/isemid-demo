package uz.uzinfocom.app.modules.act.application.handler.act155;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.act.application.handler.ActTypeHandler;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.Act155DetailResponse;
import uz.uzinfocom.app.modules.act.application.query.mapper.ActDetailMapper;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.domain.model.act155.Act155;
import uz.uzinfocom.app.modules.act.domain.model.act155.Act155Detail;
import uz.uzinfocom.app.modules.act.mapper.act155.Act155Mapper;
import uz.uzinfocom.app.modules.act.web.dto.request.Act155Request;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.persistence.sync.ChildCollectionSync;

@Component
@RequiredArgsConstructor
public class Act155Handler implements ActTypeHandler<Act155, Act155Request, Act155DetailResponse> {

    private final Act155Mapper mapper;
    private final ActDetailMapper actDetailMapper;
    private final AuditResolver auditResolver;

    @Override
    public ActType getType() {
        return ActType.ACT155;
    }

    @Override
    public Act155 createBlank() {
        return new Act155();
    }

    @Override
    public void update(Act155 act, Act155Request request) {
        mapper.copyOwnFields(act, request);

        if (act.getInstitution() != null) {
            act.getInstitution().normalize();
        }

        ChildCollectionSync.sync(
                act, act.getAct155Details(), request.samples(),
                mapper::toEntity, mapper::update, Act155Detail::setAct155
        );
    }

    @Override
    public void validate(Act155 act) {
        // No cross-field business rules beyond bean validation identified for this type.
    }

    @Override
    public Act155DetailResponse toResponse(Act155 act) {
        return (Act155DetailResponse) actDetailMapper.toDetailResponse(act, auditResolver.resolve(act));
    }
}
