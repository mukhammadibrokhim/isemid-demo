package uz.uzinfocom.isemid.features.classifier.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.ManualReportResponse;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;

@Mapper(componentModel = "spring")
public interface ManualReportMapper {

    @Mapping(target = "mkb10Codes", source = "mkb10Codes")
    ManualReportResponse toResponse(ManualReport manualReport);
}
