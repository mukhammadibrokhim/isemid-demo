package uz.uzinfocom.app.platform.reference.application.manualreport.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.dto.ManualReportLookupResponse;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.dto.ManualReportResponse;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.dto.ManualReportTableResponse;
import uz.uzinfocom.app.platform.reference.application.manualreport.query.projection.ManualReportTableProjection;
import uz.uzinfocom.app.platform.reference.domain.ManualReport;

@Mapper(componentModel = "spring", uses = ManualReportQueryMappingHelper.class)
public interface ManualReportMapper {

    ManualReportResponse toResponse(ManualReport manualReport);

    ManualReportTableResponse toTableResponse(ManualReportTableProjection projection);

    @Mapping(target = "name", source = "manualReport", qualifiedByName = "manualReportLookupName")
    ManualReportLookupResponse toLookupResponse(ManualReport manualReport);
}
