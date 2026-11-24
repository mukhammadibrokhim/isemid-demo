package uz.uzinfocom.app.modules.reference.application.population.command;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.app.modules.reference.application.population.dto.PopulationCreateRequest;
import uz.uzinfocom.app.modules.reference.application.population.dto.PopulationUpdateRequest;
import uz.uzinfocom.app.modules.reference.domain.Population;

@Mapper(componentModel = "spring")
public interface PopulationCommandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "source", constant = "MANUAL")
    @Mapping(target = "deleted", constant = "false")
    Population toEntity(PopulationCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "soatoId", ignore = true)
    @Mapping(target = "year", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(@MappingTarget Population entity, PopulationUpdateRequest request);
}
