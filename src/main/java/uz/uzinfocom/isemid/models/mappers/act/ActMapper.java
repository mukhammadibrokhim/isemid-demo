package uz.uzinfocom.isemid.models.mappers.act;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import uz.uzinfocom.isemid.models.entities.act.Act;
import uz.uzinfocom.isemid.models.entities.act.act153.Act153;
import uz.uzinfocom.isemid.models.entities.act.act154.Act154;
import uz.uzinfocom.isemid.models.entities.act.act155.Act155;
import uz.uzinfocom.isemid.models.entities.act.act156.Act156;
import uz.uzinfocom.isemid.models.payload.responses.act.RespAct;
import uz.uzinfocom.isemid.models.payload.responses.act.act153.RespAct153;
import uz.uzinfocom.isemid.models.payload.responses.act.act154.RespAct154;
import uz.uzinfocom.isemid.models.payload.responses.act.act155.RespAct155;
import uz.uzinfocom.isemid.models.payload.responses.act.act156.RespAct156;

@Mapper(componentModel = "spring")
public interface ActMapper {
    ActMapper INSTANCE = Mappers.getMapper(ActMapper.class);

    default RespAct toDto(Act act) {
        if (act == null) {
            return null;
        }
        return switch (act) {
            case Act153 act153 -> toRespAct153(act153);
            case Act154 act154 -> toRespAct154(act154);
            case Act155 act155 -> toRespAct155(act155);
            case Act156 act156 -> toRespAct156(act156);
            default -> throw new IllegalArgumentException("Unknown Act Type: " + act.getClass());
        };
    }

    @Mapping(target = "id", source = "id")
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "cardId", source = "card.id")
    @Mapping(target = "act153Details", source = "act153Details")
    RespAct153 toRespAct153(Act153 act);

    @Mapping(target = "cardId", source = "card.id")
    RespAct154 toRespAct154(Act154 act);

    @Mapping(target = "cardId", source = "card.id")
    RespAct155 toRespAct155(Act155 act);

    @Mapping(target = "cardId", source = "card.id")
    RespAct156 toRespAct156(Act156 act);
}
