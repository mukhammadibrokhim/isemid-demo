package uz.uzinfocom.app.modules.act.application.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.act.application.query.dto.ActTableResponse;
import uz.uzinfocom.app.modules.act.application.query.projection.ActTableProjection;

/**
 * Table-row mapping only — the polymorphic per-subtype detail response is
 * built by {@link ActDetailMapper} instead, since MapStruct can't dispatch a
 * single abstract {@code Act} source to one of 6 different concrete
 * subclasses of a sealed target interface on its own.
 */
@Mapper(componentModel = "spring", uses = ActTableMapperHelper.class)
public interface ActMapper {

    @Mapping(target = "status", source = "actStatus")
    @Mapping(target = "actTypeName", source = "actType", qualifiedByName = "actTypeName")
    ActTableResponse toTableResponse(ActTableProjection projection);
}
