package uz.uzinfocom.isemid.features.form058.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.isemid.features.card.mappers.CardMapper;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogConverter;
import uz.uzinfocom.isemid.features.form058.dtos.responses.Form058Response;
import uz.uzinfocom.isemid.features.form058.models.Form058;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationConverter;
import uz.uzinfocom.isemid.features.patient.mappers.PatientMapper;

@Mapper(componentModel = "spring", uses = {
        CatalogConverter.class, PatientMapper.class, OrganizationConverter.class, CardMapper.class})
public interface Form058Mapper {

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
    Form058Response toResponse(Form058 form058);
}
