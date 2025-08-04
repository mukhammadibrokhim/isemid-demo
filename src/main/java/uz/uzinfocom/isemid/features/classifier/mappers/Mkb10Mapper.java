package uz.uzinfocom.isemid.features.classifier.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.Mkb10Request;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.Mkb10Response;
import uz.uzinfocom.isemid.features.classifier.models.Mkb10;

@Mapper(componentModel = "spring")
public interface Mkb10Mapper {
    Mkb10 toEntity(Mkb10Request request);

    Mkb10Response toResponse(Mkb10 mkb10);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Mkb10 mkb10, Mkb10Request request);
}
