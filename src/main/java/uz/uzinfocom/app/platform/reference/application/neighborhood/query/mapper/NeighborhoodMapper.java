package uz.uzinfocom.app.platform.reference.application.neighborhood.query.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.app.platform.reference.domain.Neighborhood;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto.NeighborhoodResponse;

@Mapper(componentModel = "spring")
public interface NeighborhoodMapper {

    NeighborhoodResponse toResponse(Neighborhood neighborhood);
}
