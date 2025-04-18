package uz.uzinfocom.isemid.features.patient.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.isemid.features.classifier.mappers.CatalogConverter;
import uz.uzinfocom.isemid.features.patient.dtos.responses.IdentifierResponse;
import uz.uzinfocom.isemid.features.patient.models.Identifier;

@Mapper(componentModel = "spring", uses = {CatalogConverter.class})
public interface IdentifierMapper {

    @Mapping(target = "typeCode", source = "typeCode", qualifiedByName = "toCatalog")
    IdentifierResponse toIdentifierResponse(Identifier identifier);
}
