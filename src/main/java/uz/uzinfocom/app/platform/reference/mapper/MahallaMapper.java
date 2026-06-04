package uz.uzinfocom.app.platform.reference.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.app.platform.reference.domain.Mahalla;
import uz.uzinfocom.app.platform.reference.dto.MahallaResponse;

@Mapper(componentModel = "spring")
public interface MahallaMapper {

    MahallaResponse toResponse(Mahalla mahalla);
}
