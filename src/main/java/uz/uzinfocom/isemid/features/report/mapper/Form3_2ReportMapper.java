package uz.uzinfocom.isemid.features.report.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.isemid.features.report.dtos.request.Form3_2ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.response.Form3_2ReportResponse;
import uz.uzinfocom.isemid.features.report.model.Form3_2Report;

@Mapper(componentModel = "spring")
public interface Form3_2ReportMapper {
    Form3_2Report entity(Form3_2ReportRequest request);

    Form3_2ReportResponse toResponse(Form3_2Report entity);
}
