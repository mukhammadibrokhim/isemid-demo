package uz.uzinfocom.app.modules.act.mapper.act156;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.app.modules.act.domain.model.act156.Act156;
import uz.uzinfocom.app.modules.act.domain.model.act156.Act156GroupDetail;
import uz.uzinfocom.app.modules.act.domain.model.act156.Act156KitchenUtensil;
import uz.uzinfocom.app.modules.act.mapper.ActEmbeddedMapper;
import uz.uzinfocom.app.modules.act.web.dto.request.act156.Act156GroupDetailRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.act156.Act156KitchenUtensilRequest;
import uz.uzinfocom.app.modules.act.web.dto.request.Act156Request;

@Mapper(componentModel = "spring", uses = ActEmbeddedMapper.class)
public interface Act156Mapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "act156", ignore = true)
    Act156GroupDetail toEntity(Act156GroupDetailRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "act156", ignore = true)
    void update(@MappingTarget Act156GroupDetail entity, Act156GroupDetailRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "act156", ignore = true)
    Act156KitchenUtensil toEntity(Act156KitchenUtensilRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "act156", ignore = true)
    void update(@MappingTarget Act156KitchenUtensil entity, Act156KitchenUtensilRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdOrgUuid", ignore = true)
    @Mapping(target = "createdOrg", ignore = true)
    @Mapping(target = "updatedOrgUuid", ignore = true)
    @Mapping(target = "updatedOrg", ignore = true)
    @Mapping(target = "actType", ignore = true)
    @Mapping(target = "actStatus", ignore = true)
    @Mapping(target = "lisInfo", ignore = true)
    @Mapping(target = "card", ignore = true)
    @Mapping(target = "assignedById", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "resultComment", ignore = true)
    @Mapping(target = "act156KitchenUtensils", ignore = true)
    @Mapping(target = "act156GroupDetails", ignore = true)
    void copyOwnFields(@MappingTarget Act156 target, Act156Request request);
}
