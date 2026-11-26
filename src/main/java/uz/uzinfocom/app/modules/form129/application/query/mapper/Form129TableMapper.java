package uz.uzinfocom.app.modules.form129.application.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.form129.application.query.dto.Form129TableResponse;
import uz.uzinfocom.app.modules.form129.application.query.projection.Form129TableProjection;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationMappingHelper;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class, uses = OrganizationMappingHelper.class)
public interface Form129TableMapper {

    @Mapping(target = "senderOrganizationName", source = "senderOrganizationId", qualifiedByName = "activeOrganizationNameById")
    @Mapping(target = "receiverOrganizationName", source = "receiverOrganizationId", qualifiedByName = "activeOrganizationNameById")
    @Mapping(target = "patient", source = "projection")
    Form129TableResponse toTableResponse(Form129TableProjection projection);

    @Mapping(target = "id", source = "patientId")
    @Mapping(target = "firstName", source = "patientFirstName")
    @Mapping(target = "lastName", source = "patientLastName")
    @Mapping(target = "middleName", source = "patientMiddleName")
    Form129TableResponse.PatientShortResponse toPatientResponse(Form129TableProjection projection);
}
