package uz.uzinfocom.app.modules.form058.application.command.create;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class)
public interface Form058CreateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "status", constant = "SENT")
    @Mapping(target = "source", constant = "ISEMID")
    @Mapping(target = "finalMkb10Code", ignore = true)
    @Mapping(target = "finalMkb10Name", ignore = true)
    @Mapping(target = "hasLinkedCards", constant = "false")
    @Mapping(target = "assignedCardId", ignore = true)
    @Mapping(target = "cancelReason", ignore = true)
    @Mapping(target = "canceledBy", ignore = true)
    @Mapping(target = "canceledAt", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedOrganizationId", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "notApprovedReason", ignore = true)
    @Mapping(target = "location.regionCode", source = "locationRegionCode")
    @Mapping(target = "location.districtCode", source = "locationDistrictCode")
    @Mapping(target = "location.neighborhoodCode", source = "locationNeighborhoodCode")
    @Mapping(target = "location.address", source = "locationAddress")
    Form058 toEntity(CreateForm058Command command);

    CreateForm058Result toResult(Form058 form058);
}
