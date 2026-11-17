package uz.uzinfocom.app.modules.card.application.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.card.application.query.dto.CardTableResponse;
import uz.uzinfocom.app.modules.card.application.query.projection.CardTableProjection;
import uz.uzinfocom.app.modules.iam.application.shared.service.OrganizationMappingHelper;
import uz.uzinfocom.app.modules.iam.application.user.query.mapper.UserMapperHelper;

@Mapper(
        componentModel = "spring",
        uses = {CardTableMapperHelper.class, UserMapperHelper.class, OrganizationMappingHelper.class}
)
public interface CardTableMapper {

    @Mapping(target = "cardTypeName", source = "cardType", qualifiedByName = "cardTypeName")
    @Mapping(target = "statusName", source = "status", qualifiedByName = "cardStatusName")
    // Filled per page in CardQueryService via one batched user lookup (avoids a findById per row).
    @Mapping(target = "assignedBy", ignore = true)
    @Mapping(target = "formId", source = ".", qualifiedByName = "resolveFormId")
    @Mapping(target = "formType", source = ".", qualifiedByName = "resolveFormType")
    @Mapping(target = "organizationId", source = ".", qualifiedByName = "resolveOrganizationId")
    @Mapping(target = "organizationName", source = ".", qualifiedByName = "resolveOrganizationName")
    @Mapping(target = "patient", source = ".", qualifiedByName = "resolvePatientShort")
    CardTableResponse toTableResponse(CardTableProjection projection);

    CardTableResponse.PatientShortResponse toPatientShortResponse(CardTableProjection.PatientRef patient);
}
