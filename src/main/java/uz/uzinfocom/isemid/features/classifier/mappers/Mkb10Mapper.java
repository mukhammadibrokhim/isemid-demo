package uz.uzinfocom.isemid.features.classifier.mappers;

import org.mapstruct.Mapper;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.Mkb10Response;
import uz.uzinfocom.isemid.features.classifier.models.Mkb10;

@Mapper(componentModel = "spring")
public interface Mkb10Mapper {
    Mkb10Response toResponse(Mkb10 mkb10);
}
