package uz.uzinfocom.isemid.features.act.mappers;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.act.dtos.requests.act153.Act153Request;
import uz.uzinfocom.isemid.features.act.dtos.requests.act154.Act154Request;
import uz.uzinfocom.isemid.features.act.dtos.requests.act155.Act155Request;
import uz.uzinfocom.isemid.features.act.dtos.requests.act156.Act156Request;
import uz.uzinfocom.isemid.features.act.dtos.requests.act223.Act223Request;
import uz.uzinfocom.isemid.features.act.dtos.requests.act224.Act224Request;
import uz.uzinfocom.isemid.features.act.dtos.responses.ActResponse;
import uz.uzinfocom.isemid.features.act.dtos.responses.act153.Act153Response;
import uz.uzinfocom.isemid.features.act.dtos.responses.act154.Act154Response;
import uz.uzinfocom.isemid.features.act.dtos.responses.act155.Act155Response;
import uz.uzinfocom.isemid.features.act.dtos.responses.act156.Act156Response;
import uz.uzinfocom.isemid.features.act.dtos.responses.act223.Act223Response;
import uz.uzinfocom.isemid.features.act.dtos.responses.act224.Act224Response;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.act153.Act153;
import uz.uzinfocom.isemid.features.act.models.act154.Act154;
import uz.uzinfocom.isemid.features.act.models.act155.Act155;
import uz.uzinfocom.isemid.features.act.models.act156.Act156;
import uz.uzinfocom.isemid.features.act.models.act223.Act223;
import uz.uzinfocom.isemid.features.act.models.act224.Act224;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {}
)
public interface ActMapper {

    @Mapping(target = "id", source = "act.id")
    @Mapping(target = "uuid", source = "act.uuid")
    @Mapping(target = "createdAt", source = "act.createdAt")
    @Mapping(target = "updatedAt", source = "act.updatedAt")
    @Mapping(target = "actType", source = "act.actType")
    Act153Response toAct153Response(Act act);

    Act154Response toAct154Response(Act act);

    Act155Response toAct155Response(Act act);

    Act156Response toAct156Response(Act act);

    Act223Response toAct223Response(Act act);

    Act224Response toAct224Response(Act act);

    default ActResponse toResponse(Act act) {
        return switch (act.getActType()) {
            case ACT153 -> toAct153Response(act);
            case ACT154 -> toAct154Response(act);
            case ACT155 -> toAct155Response(act);
            case ACT156 -> toAct156Response(act);
            case ACT223 -> toAct223Response(act);
            case ACT224 -> toAct224Response(act);
        };
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Act153 entity, Act153Request dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Act154 entity, Act154Request dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Act155 entity, Act155Request dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Act156 entity, Act156Request dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Act223 entity, Act223Request dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Act224 entity, Act224Request dto);
}
