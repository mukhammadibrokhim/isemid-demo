package uz.uzinfocom.app.platform.reference.application.country.query.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.app.platform.reference.application.country.query.dto.CountryDetailedResponse;
import uz.uzinfocom.app.platform.reference.application.country.query.dto.CountryTableResponse;
import uz.uzinfocom.app.platform.reference.application.country.query.projection.CountryTableProjection;
import uz.uzinfocom.app.platform.reference.domain.Country;

@Mapper(componentModel = "spring")
public interface CountryMapper {

    CountryDetailedResponse toResponse(Country country);

    CountryTableResponse toTableResponse(CountryTableProjection projection);
}
