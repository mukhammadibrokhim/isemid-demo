package uz.uzinfocom.app.modules.form0581.application.query.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.form0581.application.query.dto.Form0581TableResponse;
import uz.uzinfocom.app.modules.form0581.application.query.mapper.helper.Form0581TableMapperHelper;
import uz.uzinfocom.app.modules.form0581.application.query.projection.Form0581TableProjection;
import uz.uzinfocom.app.modules.form0581.application.query.Form0581Direction;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationMappingHelper;

@Mapper(
        componentModel = "spring",
        uses = {
                Form0581TableMapperHelper.class,
                OrganizationMappingHelper.class
        }
)
public interface Form0581TableMapper {

    @Mapping(target = "status", source = ".", qualifiedByName = "toTableStatus")
    @Mapping(target = "icd10Code", source = "diagnosisInfo.icd10Code")
    @Mapping(target = "icd10Name", source = "diagnosisInfo.icd10Name")
    @Mapping(target = "senderOrganizationName", source = "senderOrganizationId", qualifiedByName = "activeOrganizationNameById")
    @Mapping(target = "receiverOrganizationName", source = "receiverOrganizationId", qualifiedByName = "activeOrganizationNameById")
    Form0581TableResponse toTableResponse(Form0581TableProjection projection, @Context Form0581Direction direction);

    // permanent* columns are filled per page by Form0581TableAddressMapperHelper#withPermanentAddresses
    @Mapping(target = "permanentRegionName", ignore = true)
    @Mapping(target = "permanentDistrictName", ignore = true)
    @Mapping(target = "permanentNeighborhoodName", ignore = true)
    @Mapping(target = "permanentStreetAddress", ignore = true)
    @Mapping(target = "permanentHouseNumber", ignore = true)
    @Mapping(target = "permanentApartmentNumber", ignore = true)
    Form0581TableResponse.PatientShortResponse toPatientShortResponse(
            Form0581TableProjection.PatientProjection patient
    );
}
