package uz.uzinfocom.isemid.features.form0581.mappers;

import org.mapstruct.Mapper;
import uz.uzinfocom.isemid.features.form0581.dtos.requests.AnimalOwnerRequest;
import uz.uzinfocom.isemid.features.form0581.dtos.responses.AnimalOwnerResponse;
import uz.uzinfocom.isemid.features.form0581.models.Form0581AnimalOwner;

@Mapper(componentModel = "spring")
public interface AnimalOwnerMapper {
    Form0581AnimalOwner toEntity(AnimalOwnerRequest request);

    AnimalOwnerResponse toResponse(Form0581AnimalOwner entity);
}
