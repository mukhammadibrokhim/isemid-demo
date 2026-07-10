package uz.uzinfocom.app.modules.form0581.application.command;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.app.modules.form0581.domain.model.Form0581OtherInjuredPerson;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

/**
 * Shared by both create and update flows — {@link #toEntity} and
 * {@link #update} are passed as the create/update functions to
 * {@link uz.uzinfocom.app.platform.persistence.sync.ChildCollectionSync#sync}
 * so a fresh entity's children and an existing entity's children go through
 * the same id-matching upsert logic.
 */
@Mapper(config = CentralMapperConfig.class)
public interface Form0581OtherInjuredPersonMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "middleName", source = "middleName")
    @Mapping(target = "address.regionCode", source = "regionCode")
    @Mapping(target = "address.districtCode", source = "districtCode")
    @Mapping(target = "address.neighborhoodCode", source = "neighborhoodCode")
    @Mapping(target = "address.street", source = "street")
    @Mapping(target = "address.houseNumber", source = "houseNumber")
    @Mapping(target = "address.apartmentNumber", source = "apartmentNumber")
    Form0581OtherInjuredPerson toEntity(OtherInjuredPersonCommand command);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "middleName", source = "middleName")
    @Mapping(target = "address.regionCode", source = "regionCode")
    @Mapping(target = "address.districtCode", source = "districtCode")
    @Mapping(target = "address.neighborhoodCode", source = "neighborhoodCode")
    @Mapping(target = "address.street", source = "street")
    @Mapping(target = "address.houseNumber", source = "houseNumber")
    @Mapping(target = "address.apartmentNumber", source = "apartmentNumber")
    void update(@MappingTarget Form0581OtherInjuredPerson entity, OtherInjuredPersonCommand command);
}
