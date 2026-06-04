package uz.uzinfocom.app.platform.reference.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.app.platform.reference.domain.Country;
import uz.uzinfocom.app.platform.reference.dto.CountryResponse;

@Mapper(componentModel = "spring")
public interface CountryMapper {

    CountryResponse toResponse(Country country);
}
