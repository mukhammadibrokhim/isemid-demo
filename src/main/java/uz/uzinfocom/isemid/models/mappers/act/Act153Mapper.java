package uz.uzinfocom.isemid.models.mappers.act;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import uz.uzinfocom.isemid.models.entities.act.act153.Act153;
import uz.uzinfocom.isemid.models.payload.responses.act.act153.RespAct153;
@Mapper(componentModel = "spring")
public interface Act153Mapper {
    Act153Mapper INSTANCE = Mappers.getMapper(Act153Mapper.class);

//    @Mapping(target = "hospitalizationDate", source = "hospitalizationDate", qualifiedByName = "mapHospitalizationDate")
//    @Mapping(target = "act153Details", source = "act153Details")
    @Mapping(target = "tin", source = "tin")
    RespAct153 toDto(Act153 act153);
}