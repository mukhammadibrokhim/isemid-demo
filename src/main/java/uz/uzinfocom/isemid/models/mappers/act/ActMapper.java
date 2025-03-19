package uz.uzinfocom.isemid.models.mappers.act;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.SubclassMapping;
import org.mapstruct.factory.Mappers;
import uz.uzinfocom.isemid.models.entities.act.Act;
import uz.uzinfocom.isemid.models.entities.act.act153.Act153;
import uz.uzinfocom.isemid.models.entities.act.act154.Act154;
import uz.uzinfocom.isemid.models.payload.responses.act.RespAct;
import uz.uzinfocom.isemid.models.payload.responses.act.RespAct153;
import uz.uzinfocom.isemid.models.payload.responses.act.RespAct154;

@Mapper(componentModel = "spring", uses = {Act153Mapper.class, Act154Mapper.class})
public interface ActMapper {
    ActMapper INSTANCE = Mappers.getMapper(ActMapper.class);

    @Mapping(source = "actType", target = "actType")
    @Mapping(source = "actStatus", target = "actStatus")
    @Mapping(source = "card.id", target = "cardId")
    @SubclassMapping(source = Act153.class, target = RespAct153.class)
    @SubclassMapping(source = Act154.class, target = RespAct154.class)
    RespAct toDto(Act act);

}
