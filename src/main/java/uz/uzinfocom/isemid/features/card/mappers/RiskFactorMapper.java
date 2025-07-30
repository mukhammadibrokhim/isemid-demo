package uz.uzinfocom.isemid.features.card.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.isemid.features.card.dtos.requests.card161.RiskFactorRequest;
import uz.uzinfocom.isemid.features.card.dtos.responses.card161.RiskFactorResponse;
import uz.uzinfocom.isemid.features.card.models.card161.RiskFactor;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogConverter;

@Mapper(componentModel = "spring", uses = CatalogConverter.class)
public interface RiskFactorMapper {

    RiskFactor toEntity(RiskFactorRequest request);

    @Mapping(target = "riskFactor",source = "riskFactorCode", qualifiedByName = "toCatalog")
    RiskFactorResponse toResponse(RiskFactor riskFactor);
}
