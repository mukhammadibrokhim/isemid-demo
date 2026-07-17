package uz.uzinfocom.app.modules.form0581.application.query.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.form0581.application.query.dto.Form0581TableResponse;
import uz.uzinfocom.app.modules.form0581.application.query.mapper.helper.Form0581TableAddressMapperHelper;
import uz.uzinfocom.app.modules.form0581.application.query.mapper.helper.Form0581TableMapperHelper;
import uz.uzinfocom.app.modules.form0581.application.query.projection.Form0581TableProjection;
import uz.uzinfocom.app.modules.form0581.web.dto.request.enums.Form0581Direction;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationMappingHelper;

@Mapper(
        componentModel = "spring",
        uses = {
                Form0581TableAddressMapperHelper.class,
                Form0581TableMapperHelper.class,
                OrganizationMappingHelper.class
        }
)
public interface Form0581TableMapper {

    @Mapping(target = "status", source = ".", qualifiedByName = "toTableStatus")
    @Mapping(target = "mkb10Code", source = "diagnosisInfo.mkb10Code")
    @Mapping(target = "mkb10Name", source = "diagnosisInfo.mkb10Name")
    @Mapping(target = "senderOrganizationName", source = "senderOrganizationId", qualifiedByName = "activeOrganizationNameById")
    Form0581TableResponse toTableResponse(Form0581TableProjection projection, @Context Form0581Direction direction);

    @Mapping(target = "permanentRegionName", source = ".", qualifiedByName = "permanentRegionName")
    @Mapping(target = "permanentDistrictName", source = ".", qualifiedByName = "permanentDistrictName")
    @Mapping(target = "permanentNeighborhoodName", source = ".", qualifiedByName = "permanentNeighborhoodName")
    @Mapping(target = "permanentStreetAddress", source = ".", qualifiedByName = "permanentStreetAddress")
    @Mapping(target = "permanentHouseNumber", source = ".", qualifiedByName = "permanentHouseNumber")
    @Mapping(target = "permanentApartmentNumber", source = ".", qualifiedByName = "permanentApartmentNumber")
    Form0581TableResponse.PatientShortResponse toPatientShortResponse(
            Form0581TableProjection.PatientProjection patient
    );
}
