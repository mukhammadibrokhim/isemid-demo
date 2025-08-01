package uz.uzinfocom.isemid.features.form058.mappers;

import org.mapstruct.Mapper;
import uz.uzinfocom.isemid.features.form058.dtos.requests.LocationRequest;
import uz.uzinfocom.isemid.features.form058.models.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    Location toEntity(LocationRequest request);
}
