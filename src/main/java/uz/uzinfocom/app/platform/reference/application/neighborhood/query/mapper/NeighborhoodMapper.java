package uz.uzinfocom.app.platform.reference.application.neighborhood.query.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto.NeighborhoodResponse;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.dto.NeighborhoodTableResponse;
import uz.uzinfocom.app.platform.reference.application.neighborhood.query.projection.NeighborhoodTableProjection;
import uz.uzinfocom.app.platform.reference.domain.Neighborhood;

@Mapper(componentModel = "spring")
public interface NeighborhoodMapper {

    NeighborhoodResponse toResponse(Neighborhood neighborhood);

    NeighborhoodTableResponse toTableResponse(NeighborhoodTableProjection projection);
}
