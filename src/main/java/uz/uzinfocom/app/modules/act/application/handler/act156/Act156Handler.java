package uz.uzinfocom.app.modules.act.application.handler.act156;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.uzinfocom.app.modules.act.application.handler.ActTypeHandler;
import uz.uzinfocom.app.modules.act.application.query.dto.detail.Act156DetailResponse;
import uz.uzinfocom.app.modules.act.application.query.mapper.ActDetailMapper;
import uz.uzinfocom.app.modules.act.domain.enums.ActType;
import uz.uzinfocom.app.modules.act.domain.model.act156.Act156;
import uz.uzinfocom.app.modules.act.domain.model.act156.Act156GroupDetail;
import uz.uzinfocom.app.modules.act.domain.model.act156.Act156KitchenUtensil;
import uz.uzinfocom.app.modules.act.mapper.act156.Act156Mapper;
import uz.uzinfocom.app.modules.act.web.dto.request.Act156Request;
import uz.uzinfocom.app.platform.iam.application.shared.service.AuditResolver;
import uz.uzinfocom.app.platform.persistence.sync.ChildCollectionSync;

@Component
@RequiredArgsConstructor
public class Act156Handler implements ActTypeHandler<Act156, Act156Request, Act156DetailResponse> {

    private final Act156Mapper mapper;
    private final ActDetailMapper actDetailMapper;
    private final AuditResolver auditResolver;

    @Override
    public ActType getType() {
        return ActType.ACT156;
    }

    @Override
    public Act156 createBlank() {
        return new Act156();
    }

    @Override
    public void update(Act156 act, Act156Request request) {
        mapper.copyOwnFields(act, request);

        if (act.getInstitution() != null) {
            act.getInstitution().normalize();
        }

        ChildCollectionSync.sync(
                act, act.getAct156KitchenUtensils(), request.kitchenUtensils(),
                mapper::toEntity, mapper::update, Act156KitchenUtensil::setAct156
        );
        ChildCollectionSync.sync(
                act, act.getAct156GroupDetails(), request.groupDetails(),
                mapper::toEntity, mapper::update, Act156GroupDetail::setAct156
        );
    }

    @Override
    public void validate(Act156 act) {
        // No cross-field business rules beyond bean validation identified for this type.
    }

    @Override
    public Act156DetailResponse toResponse(Act156 act) {
        return (Act156DetailResponse) actDetailMapper.toDetailResponse(act, auditResolver.resolve(act));
    }
}
