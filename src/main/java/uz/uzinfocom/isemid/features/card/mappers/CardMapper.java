package uz.uzinfocom.isemid.features.card.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.isemid.features.card.dtos.responses.CardResponse;
import uz.uzinfocom.isemid.features.card.dtos.responses.card161.Card161Response;
import uz.uzinfocom.isemid.features.card.dtos.responses.card174.Card174Response;
import uz.uzinfocom.isemid.features.card.dtos.responses.card175.Card175Response;
import uz.uzinfocom.isemid.features.card.dtos.responses.card205.Card205Response;
import uz.uzinfocom.isemid.features.card.dtos.responses.card_tube.CardTubeResponse;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.card.models.card161.Card161;
import uz.uzinfocom.isemid.features.card.models.card174.Card174;
import uz.uzinfocom.isemid.features.card.models.card175.Card175;
import uz.uzinfocom.isemid.features.card.models.card205.Card205;
import uz.uzinfocom.isemid.features.card.models.card_tube.CardTube;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogConverter;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationConverter;
import uz.uzinfocom.isemid.features.patient.mappers.AddressMapper;
import uz.uzinfocom.isemid.features.patient.mappers.IdentifierMapper;

@Mapper(componentModel = "spring", uses = {
        OrganizationConverter.class,
        CatalogConverter.class,
        IdentifierMapper.class,
        AddressMapper.class,
        Card161RiskFactorMapper.class
})
public interface CardMapper {

    @Mapping(target = "id", source = "card.id")
    @Mapping(target = "uuid", source = "card.uuid")  // Assuming Card has uuid field
    @Mapping(target = "createdAt", source = "card.createdAt")
    @Mapping(target = "updatedAt", source = "card.updatedAt")
    @Mapping(target = "cardType", source = "card.cardType")
    @Mapping(target = "users", source = "card.users")
    @Mapping(target = "form058", source = "card.form058")
    @Mapping(target = "status", source = "card.status")
    @Mapping(target = "region", source = "regionCode", qualifiedByName = "toCatalog")
    @Mapping(target = "district", source = "districtCode", qualifiedByName = "toCatalog")
    @Mapping(target = "detected", source = "detectedCode", qualifiedByName = "toCatalog")
    @Mapping(target = "deliveryMethod", source = "deliveryMethodCode", qualifiedByName = "toCatalog")
    @Mapping(target = "homeStayExclusionReason", source = "homeStayExclusionReasonCode", qualifiedByName = "toCatalog")
    @Mapping(target = "lateAdmissionReason", source = "lateAdmissionReasonCode", qualifiedByName = "toCatalog")
    @Mapping(target = "diagnosisVerified", source = "diagnosisVerifiedCode", qualifiedByName = "toCatalog")
    @Mapping(target = "livingCondition", source = "livingConditionCode", qualifiedByName = "toCatalog")
    @Mapping(target = "waterSupply", source = "waterSupplyCode", qualifiedByName = "toCatalog")
    @Mapping(target = "liquidWasteDisposalType", source = "liquidWasteDisposalTypeCode", qualifiedByName = "toCatalog")
    @Mapping(target = "solidWasteDisposalType", source = "solidWasteDisposalTypeCode", qualifiedByName = "toCatalog")
    @Mapping(target = "roomCondition", source = "roomConditionCode", qualifiedByName = "toCatalog")
    @Mapping(target = "yardCondition", source = "yardConditionCode", qualifiedByName = "toCatalog")
    @Mapping(target = "areaCondition", source = "areaConditionCode", qualifiedByName = "toCatalog")
    @Mapping(target = "importantCausesOfDisease", source = "importantCausesOfDiseaseCode", qualifiedByName = "toCatalog")
    @Mapping(target = "visitedObjects", source = "visitedObjectsCode", qualifiedByName = "toCatalog")
    @Mapping(target = "infectionLocation", source = "infectionLocationCode", qualifiedByName = "toCatalog")
    @Mapping(target = "probableInfectionLocation", source = "probableInfectionLocationCode", qualifiedByName = "toCatalog")
    @Mapping(target = "mainProbableInfectionFactor", source = "mainProbableInfectionFactorCode", qualifiedByName = "toCatalog")
    @Mapping(target = "outbreakInfection", source = "outbreakInfectionCode", qualifiedByName = "toCatalog")
    @Mapping(target = "caseStatus", source = "caseStatusCode", qualifiedByName = "toCatalog")
    @Mapping(target = "infectionSourceDetail.animalType", source = "infectionSourceDetail.animalTypeCode", qualifiedByName = "toCatalog")
    @Mapping(target = "infectionSourceDetail.infectionSourceNotFound", source = "infectionSourceDetail.infectionSourceNotFoundCode", qualifiedByName = "toCatalog")
    @Mapping(target = "riskFactors", source = "riskFactors")
    @Mapping(target = "form058.status", source = "card.form058.status")
    @Mapping(target = "form058.mkb10Code", source = "card.form058.mkb10Code")
    @Mapping(target = "form058.mkb10Name", source = "card.form058.mkb10Name")
    @Mapping(target = "form058.docSendDate", source = "card.form058.docSendDate")
    @Mapping(target = "form058.notifierFullName", source = "card.form058.notifierFullName")
    @Mapping(target = "form058.cardByFullName", source = "card.form058.cardByFullName")
    @Mapping(target = "form058.senderOrganization", source = "card.form058.senderOrganizationId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "form058.receiverOrganization", source = "card.form058.receiverOrganizationId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "form058.hospitalPlace", source = "card.form058.hospitalPlaceId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "form058.patient.gender", source = "card.form058.patient.genderCode", qualifiedByName = "toCatalog")
    @Mapping(target = "form058.patient.identifiers", source = "card.form058.patient.identifiers")
    @Mapping(target = "form058.patient.addresses", source = "card.form058.patient.addresses")
    Card161Response toCard161Response(Card161 card);

    @Mapping(target = "id", source = "card.id")
    @Mapping(target = "uuid", source = "card.uuid")  // Assuming Card has uuid field
    @Mapping(target = "createdAt", source = "card.createdAt")
    @Mapping(target = "updatedAt", source = "card.updatedAt")
    @Mapping(target = "cardType", source = "card.cardType")
    @Mapping(target = "users", source = "card.users")
    @Mapping(target = "form058", source = "card.form058")
    @Mapping(target = "status", source = "card.status")
    Card174Response toCard174Response(Card174 card);

    @Mapping(target = "id", source = "card.id")
    @Mapping(target = "uuid", source = "card.uuid")  // Assuming Card has uuid field
    @Mapping(target = "createdAt", source = "card.createdAt")
    @Mapping(target = "updatedAt", source = "card.updatedAt")
    @Mapping(target = "cardType", source = "card.cardType")
    @Mapping(target = "users", source = "card.users")
    @Mapping(target = "form058", source = "card.form058")
    @Mapping(target = "status", source = "card.status")
    Card175Response toCard175Response(Card175 card);

    @Mapping(target = "id", source = "card.id")
    @Mapping(target = "uuid", source = "card.uuid")  // Assuming Card has uuid field
    @Mapping(target = "createdAt", source = "card.createdAt")
    @Mapping(target = "updatedAt", source = "card.updatedAt")
    @Mapping(target = "cardType", source = "card.cardType")
    @Mapping(target = "users", source = "card.users")
    @Mapping(target = "form058", source = "card.form058")
    @Mapping(target = "status", source = "card.status")
    Card205Response toCard205Response(Card205 card);

    @Mapping(target = "id", source = "card.id")
    @Mapping(target = "uuid", source = "card.uuid")  // Assuming Card has uuid field
    @Mapping(target = "createdAt", source = "card.createdAt")
    @Mapping(target = "updatedAt", source = "card.updatedAt")
    @Mapping(target = "cardType", source = "card.cardType")
    @Mapping(target = "users", source = "card.users")
    @Mapping(target = "form058", source = "card.form058")
    @Mapping(target = "status", source = "card.status")
    CardTubeResponse toCardTubeResponse(CardTube card);


    default CardResponse toCardResponse(Card card) {
        return switch (card.getCardType()) {
            case CARD161 -> toCard161Response((Card161) card);
            case CARD174 -> toCard174Response((Card174) card);
            case CARD175 -> toCard175Response((Card175) card);
            case CARD205 -> toCard205Response((Card205) card);
            case CARD_TUBE -> toCardTubeResponse((CardTube) card);
        };
    }
}
