package uz.uzinfocom.isemid.features.form058.mappers;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.card.mappers.CardMapper;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogMapperHelper;
import uz.uzinfocom.isemid.features.form058.dtos.requests.Form058Request;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058DetailsResponse;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058Response;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapperHelper;
import uz.uzinfocom.isemid.features.patient.mappers.PatientMapper;
import uz.uzinfocom.isemid.features.user.mappers.helper.UserMapperHelper;

@Mapper(componentModel = "spring", uses = {
        Form058MapperHelper.class,
        CatalogMapperHelper.class,
        PatientMapper.class,
        LocationMapper.class,
        OrganizationMapperHelper.class,
        CardMapper.class,
        UserMapperHelper.class
})
public interface Form058Mapper {

    @Mapping(source = "hospitalPlaceId", target = "hospitalPlaceId", qualifiedByName = "uuidToLongId")
    @Mapping(source = "senderOrganizationId", target = "senderOrganizationId", qualifiedByName = "uuidToLongId")
    @Mapping(source = "receiverOrganizationId", target = "receiverOrganizationId", qualifiedByName = "uuidToLongId")
    Form058 toEntity(Form058Request request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "finalMkb10Code", source = "finalMkb10Code")
    @Mapping(target = "finalMkb10Name", source = "finalMkb10Name")
    @Mapping(target = "mkb10UsageLimit", source = "mkb10UsageLimit")
    @Mapping(target = "patient", source = "patient")
    @Mapping(target = "diseasePlace", source = "diseasePlaceCode", qualifiedByName = "toCatalog")
    @Mapping(target = "senderOrganization", source = "senderOrganizationId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "receiverOrganization", source = "receiverOrganizationId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "hospitalPlace", source = "hospitalPlaceId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "cards", source = "cards")
    @Mapping(target = "canceledBy", source = "canceledBy", qualifiedByName = "toUserMiniResponse")
    Form058Response toResponse(Form058 form058);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "finalMkb10Code", source = "finalMkb10Code")
    @Mapping(target = "finalMkb10Name", source = "finalMkb10Name")
    @Mapping(target = "mkb10UsageLimit", source = "mkb10UsageLimit")
    @Mapping(target = "patient", source = "patient")
    @Mapping(target = "diseasePlace", source = "diseasePlaceCode", qualifiedByName = "toCatalog")
    @Mapping(target = "senderOrganization", source = "senderOrganizationId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "receiverOrganization", source = "receiverOrganizationId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "hospitalPlace", source = "hospitalPlaceId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "cards", source = "cards")
    @Mapping(target = "affiliation", source = ".", qualifiedByName = "mapAffiliation")
    @Mapping(target = "canceledBy", source = "canceledBy", qualifiedByName = "toUserMiniResponse")
    Form058Response toResponse(Form058 form058, @Context Long orgId);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "hospitalPlaceId", target = "hospitalPlaceId", qualifiedByName = "uuidToLongId")
    @Mapping(source = "senderOrganizationId", target = "senderOrganizationId", qualifiedByName = "uuidToLongId")
    @Mapping(source = "receiverOrganizationId", target = "receiverOrganizationId", qualifiedByName = "uuidToLongId")
    void update(@MappingTarget Form058 existedForm058, Form058Request request);

    @Mapping(target = "finalMkb10Code", source = "finalMkb10Code")
    @Mapping(target = "finalMkb10Name", source = "finalMkb10Name")
    @Mapping(target = "mkb10UsageLimit", source = "mkb10UsageLimit")
    @Mapping(target = "patient", source = "patient")
    @Mapping(target = "diseasePlace", source = "diseasePlaceCode", qualifiedByName = "toCatalog")
    @Mapping(target = "senderOrganization", source = "senderOrganizationId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "receiverOrganization", source = "receiverOrganizationId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "hospitalPlace", source = "hospitalPlaceId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "canceledBy", source = "canceledBy", qualifiedByName = "toUserMiniResponse")
    @Mapping(target = "cards", source = "cards")
    Form058DetailsResponse toDetailsResponse(Form058 form);
}
