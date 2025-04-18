package uz.uzinfocom.isemid.features.card.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.isemid.features.card.dtos.responses.card161.Card161RiskFactorResponse;
import uz.uzinfocom.isemid.features.card.models.card161.Card161RiskFactor;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogConverter;

@Mapper(componentModel = "spring", uses = CatalogConverter.class)
public interface Card161RiskFactorMapper {
    @Mapping(target = "riskFactor",source = "riskFactorCode", qualifiedByName = "toCatalog")
    Card161RiskFactorResponse toResponse(Card161RiskFactor card161RiskFactor);
}
