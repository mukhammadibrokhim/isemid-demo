package uz.uzinfocom.isemid.features.act.mappers;


import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import uz.uzinfocom.isemid.features.act.dtos.LisActPushRequest;
import uz.uzinfocom.isemid.features.act.dtos.LisRequest;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EMisToLisRequestMapper {

  LisActPushRequest map(LisRequest lisActPushRequest);

}
