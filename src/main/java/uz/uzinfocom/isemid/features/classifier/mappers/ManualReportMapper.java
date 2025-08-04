package uz.uzinfocom.isemid.features.classifier.mappers;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.classifier.dtos.requests.ManualReportRequest;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.ManualReportResponse;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;

@Mapper(componentModel = "spring", uses = { Mkb10MapperHelper.class })
public interface ManualReportMapper {

    ManualReport toEntity(ManualReportRequest request);

    @Mapping(target = "mkb10Codes", source = "mkb10Codes", qualifiedByName = "mapMkb10Codes")
    ManualReportResponse toResponse(ManualReport manualReport);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget ManualReport entity, ManualReportRequest request);
}
