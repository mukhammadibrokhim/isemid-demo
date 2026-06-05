package uz.uzinfocom.app.platform.reference.application.country.query.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.app.platform.reference.application.country.query.dto.CountryResponse;
import uz.uzinfocom.app.platform.reference.application.country.query.dto.CountryTableResponse;
import uz.uzinfocom.app.platform.reference.application.country.query.projection.CountryTableProjection;
import uz.uzinfocom.app.platform.reference.domain.Country;

@Mapper(componentModel = "spring")
public interface CountryMapper {

    CountryResponse toResponse(Country country);

    CountryTableResponse toTableResponse(CountryTableProjection projection);
}
