package uz.uzinfocom.isemid.features.classifier.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.isemid.features.classifier.dtos.responses.ManualReportResponse;
import uz.uzinfocom.isemid.features.classifier.models.ManualReport;

@Mapper(componentModel = "spring", uses = { Mkb10Converter.class })
public interface ManualReportMapper {

    @Mapping(target = "mkb10Codes", source = "mkb10Codes", qualifiedByName = "mapMkb10Codes")
    ManualReportResponse toResponse(ManualReport manualReport);
}
