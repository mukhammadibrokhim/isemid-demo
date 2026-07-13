package uz.uzinfocom.isemid.features.form0581.mappers;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.card.mappers.CardMapper;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogMapperHelper;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.Form0581Request;
import uz.uzinfocom.isemid.features.form0581.dtos.responses.Form0581DetailsResponse;
import uz.uzinfocom.isemid.features.form0581.dtos.responses.Form0581Response;
import uz.uzinfocom.isemid.features.form0581.models.Form0581;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapperHelper;
import uz.uzinfocom.isemid.features.patient.mappers.PatientMapper;
import uz.uzinfocom.isemid.features.user.mappers.helper.UserMapperHelper;

@Mapper(componentModel = "spring", uses = {
        Form0581MapperHelper.class,
        CatalogMapperHelper.class,
        PatientMapper.class,
        OrganizationMapperHelper.class,
        CardMapper.class,
        UserMapperHelper.class,
        AnimalOwnerMapper.class,
        BittenPersonMapper.class
})
public interface Form0581Mapper {

    @Mapping(source = "hospitalPlaceId", target = "hospitalPlaceId", qualifiedByName = "uuidToLongId")
    @Mapping(source = "senderOrganizationId", target = "senderOrganizationId", qualifiedByName = "uuidToLongId")
    @Mapping(source = "receiverOrganizationId", target = "receiverOrganizationId", qualifiedByName = "uuidToLongId")
    @Mapping(target = "bittenPersons", source = "bittenPersons")
    @Mapping(target = "diagnosisApproval.finalMkb10Code", source = "finalMkb10Code")
    @Mapping(target = "diagnosisApproval.finalMkb10Name", source = "finalMkb10Name")
    @Mapping(target = "injuryLocation.stateCode", source = "injuryStateCode")
    @Mapping(target = "injuryLocation.cityCode", source = "injuryCityCode")
    @Mapping(target = "injuryLocation.address", source = "injuryAddress")
    @Mapping(target = "animalInfo.categoryCode", source = "animalCategoryCode")
    @Mapping(target = "animalInfo.color", source = "animalColor")
    @Mapping(target = "animalInfo.type", source = "animalType")
    @Mapping(target = "animalInfo.breed", source = "animalBreed")
    Form0581 toEntity(Form0581Request request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "patient", source = "patient")
    @Mapping(target = "senderOrganization", source = "senderOrganizationId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "receiverOrganization", source = "receiverOrganizationId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "hospitalPlace", source = "hospitalPlaceId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "animalOwner", source = "animalOwner")
    @Mapping(target = "bittenPersons", source = "bittenPersons")
    @Mapping(target = "canceledBy", source = "canceledBy", qualifiedByName = "toUserMiniResponse")
    @Mapping(target = "finalMkb10Code", source = "diagnosisApproval.finalMkb10Code")
    @Mapping(target = "finalMkb10Name", source = "diagnosisApproval.finalMkb10Name")
    @Mapping(target = "approvedFullName", source = "diagnosisApproval.approvedFullName")
    @Mapping(target = "approvedOrg", source = "diagnosisApproval.approvedOrgUuid", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "notApproveComment", source = "diagnosisApproval.notApproveComment")
    @Mapping(target = "injuryStateCode", source = "injuryLocation.stateCode")
    @Mapping(target = "injuryCityCode", source = "injuryLocation.cityCode")
    @Mapping(target = "injuryAddress", source = "injuryLocation.address")
    @Mapping(target = "animalCategoryCode", source = "animalInfo.categoryCode")
    @Mapping(target = "animalColor", source = "animalInfo.color")
    @Mapping(target = "animalType", source = "animalInfo.type")
    @Mapping(target = "animalBreed", source = "animalInfo.breed")
    Form0581Response toResponse(Form0581 form0581);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "patient", source = "patient")
    @Mapping(target = "senderOrganization", source = "senderOrganizationId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "receiverOrganization", source = "receiverOrganizationId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "hospitalPlace", source = "hospitalPlaceId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "animalOwner", source = "animalOwner")
    @Mapping(target = "bittenPersons", source = "bittenPersons")
    @Mapping(target = "affiliation", source = ".", qualifiedByName = "mapAffiliation")
    @Mapping(target = "canceledBy", source = "canceledBy", qualifiedByName = "toUserMiniResponse")
    @Mapping(target = "finalMkb10Code", source = "diagnosisApproval.finalMkb10Code")
    @Mapping(target = "finalMkb10Name", source = "diagnosisApproval.finalMkb10Name")
    @Mapping(target = "approvedFullName", source = "diagnosisApproval.approvedFullName")
    @Mapping(target = "approvedOrg", source = "diagnosisApproval.approvedOrgUuid", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "notApproveComment", source = "diagnosisApproval.notApproveComment")
    @Mapping(target = "injuryStateCode", source = "injuryLocation.stateCode")
    @Mapping(target = "injuryCityCode", source = "injuryLocation.cityCode")
    @Mapping(target = "injuryAddress", source = "injuryLocation.address")
    @Mapping(target = "animalCategoryCode", source = "animalInfo.categoryCode")
    @Mapping(target = "animalColor", source = "animalInfo.color")
    @Mapping(target = "animalType", source = "animalInfo.type")
    @Mapping(target = "animalBreed", source = "animalInfo.breed")
    Form0581Response toResponse(Form0581 form0581, @Context Long orgId);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "hospitalPlaceId", target = "hospitalPlaceId", qualifiedByName = "uuidToLongId")
    @Mapping(source = "senderOrganizationId", target = "senderOrganizationId", qualifiedByName = "uuidToLongId")
    @Mapping(source = "receiverOrganizationId", target = "receiverOrganizationId", qualifiedByName = "uuidToLongId")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "bittenPersons", ignore = true)
    @Mapping(target = "diagnosisApproval.finalMkb10Code", source = "finalMkb10Code")
    @Mapping(target = "diagnosisApproval.finalMkb10Name", source = "finalMkb10Name")
    @Mapping(target = "injuryLocation.stateCode", source = "injuryStateCode")
    @Mapping(target = "injuryLocation.cityCode", source = "injuryCityCode")
    @Mapping(target = "injuryLocation.address", source = "injuryAddress")
    @Mapping(target = "animalInfo.categoryCode", source = "animalCategoryCode")
    @Mapping(target = "animalInfo.color", source = "animalColor")
    @Mapping(target = "animalInfo.type", source = "animalType")
    @Mapping(target = "animalInfo.breed", source = "animalBreed")
    void update(@MappingTarget Form0581 existedForm0581, Form0581Request request);

    @Mapping(target = "patient", source = "patient")
    @Mapping(target = "senderOrganization", source = "senderOrganizationId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "receiverOrganization", source = "receiverOrganizationId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "hospitalPlace", source = "hospitalPlaceId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "canceledBy", source = "canceledBy", qualifiedByName = "toUserMiniResponse")
    @Mapping(target = "finalMkb10Code", source = "diagnosisApproval.finalMkb10Code")
    @Mapping(target = "finalMkb10Name", source = "diagnosisApproval.finalMkb10Name")
    @Mapping(target = "approvedFullName", source = "diagnosisApproval.approvedFullName")
    @Mapping(target = "approvedOrg", source = "diagnosisApproval.approvedOrgUuid", qualifiedByName = "toOrgMiniResponse")
    Form0581DetailsResponse toDetailsResponse(Form0581 form);
}
