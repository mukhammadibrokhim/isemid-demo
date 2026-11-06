package uz.uzinfocom.app.modules.form058.application.query.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.form058.application.query.dto.Form058TableResponse;
import uz.uzinfocom.app.modules.form058.application.query.mapper.helper.Form058TableMapperHelper;
import uz.uzinfocom.app.modules.form058.application.query.projection.Form058TableProjection;
import uz.uzinfocom.app.modules.form058.application.query.Form058Direction;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationMappingHelper;

@Mapper(
        componentModel = "spring",
        uses = {
                Form058TableMapperHelper.class,
                OrganizationMappingHelper.class
        }
)
public interface Form058TableMapper {

    @Mapping(target = "status", source = ".", qualifiedByName = "toTableStatus")
    @Mapping(target = "icd10Code", source = "diagnosisInfo.icd10Code")
    @Mapping(target = "icd10Name", source = "diagnosisInfo.icd10Name")
    @Mapping(target = "senderOrganizationName",source = "senderOrganizationId",qualifiedByName = "activeOrganizationNameById")
    @Mapping(target = "receiverOrganizationName", source = "receiverOrganizationId", qualifiedByName = "activeOrganizationNameById")
    Form058TableResponse toTableResponse(Form058TableProjection projection, @Context Form058Direction direction);

    // permanent* columns are filled per page by Form058TableAddressMapperHelper#withPermanentAddresses
    @Mapping(target = "permanentRegionName", ignore = true)
    @Mapping(target = "permanentDistrictName", ignore = true)
    @Mapping(target = "permanentNeighborhoodName", ignore = true)
    @Mapping(target = "permanentStreetAddress", ignore = true)
    @Mapping(target = "permanentHouseNumber", ignore = true)
    @Mapping(target = "permanentApartmentNumber", ignore = true)
    Form058TableResponse.PatientShortResponse toPatientShortResponse(
            Form058TableProjection.PatientProjection patient
    );
}