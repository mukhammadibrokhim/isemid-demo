package uz.uzinfocom.isemid.features.act.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

@Mapper(componentModel = "spring")
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
            case ACT153 -> toAct153Response((Act153) act);
            case ACT154 -> toAct154Response((Act154) act);
            case ACT155 -> toAct155Response((Act155) act);
            case ACT156 -> toAct156Response((Act156) act);
            case ACT223 -> toAct223Response((Act223) act);
            case ACT224 -> toAct224Response((Act224) act);
        };
    }
}
