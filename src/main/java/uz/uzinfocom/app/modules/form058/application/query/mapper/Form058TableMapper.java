package uz.uzinfocom.app.modules.form058.application.query.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.form058.application.query.dto.Form058TableResponse;
import uz.uzinfocom.app.modules.form058.application.query.mapper.helper.Form058TableAddressMapperHelper;
import uz.uzinfocom.app.modules.form058.application.query.mapper.helper.Form058TableMapperHelper;
import uz.uzinfocom.app.modules.form058.application.query.projection.Form058TableProjection;
import uz.uzinfocom.app.modules.form058.application.shared.OrganizationMappingHelper;
import uz.uzinfocom.app.modules.form058.web.request.enums.Form058Direction;

@Mapper(
        componentModel = "spring",
        uses = {
                Form058TableAddressMapperHelper.class,
                Form058TableMapperHelper.class,
                OrganizationMappingHelper.class
        }
)
public interface Form058TableMapper {

    @Mapping(target = "status", source = ".", qualifiedByName = "toTableStatus")
    @Mapping(target = "mkb10Code", source = "diagnosisInfo.mkb10Code")
    @Mapping(target = "mkb10Name", source = "diagnosisInfo.mkb10Name")
    @Mapping(target = "senderOrganizationName",source = "senderOrganizationId",qualifiedByName = "activeOrganizationNameById")
    Form058TableResponse toTableResponse(Form058TableProjection projection, @Context Form058Direction direction);

    @Mapping(target = "permanentRegionName", source = ".", qualifiedByName = "permanentRegionName")
    @Mapping(target = "permanentDistrictName", source = ".", qualifiedByName = "permanentDistrictName")
    @Mapping(target = "permanentNeighborhoodName", source = ".", qualifiedByName = "permanentNeighborhoodName")
    @Mapping(target = "permanentStreetAddress", source = ".", qualifiedByName = "permanentStreetAddress")
    @Mapping(target = "permanentHouseNumber", source = ".", qualifiedByName = "permanentHouseNumber")
    @Mapping(target = "permanentApartmentNumber", source = ".", qualifiedByName = "permanentApartmentNumber")
    Form058TableResponse.PatientShortResponse toPatientShortResponse(
            Form058TableProjection.PatientProjection patient
    );
}