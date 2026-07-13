package uz.uzinfocom.isemid.features.form0581.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.BittenPersonRequest;
import uz.uzinfocom.isemid.features.form0581.dtos.responses.BittenPersonResponse;
import uz.uzinfocom.isemid.features.form0581.models.Form0581BittenPerson;

@Mapper(componentModel = "spring")
public interface BittenPersonMapper {
    @Mapping(target = "form0581", ignore = true)
    Form0581BittenPerson toEntity(BittenPersonRequest request);

    BittenPersonResponse toResponse(Form0581BittenPerson entity);
}
