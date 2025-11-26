package uz.uzinfocom.isemid.features.report.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.isemid.features.report.dtos.request.Form3_1ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.response.Form3_1ReportResponse;
import uz.uzinfocom.isemid.features.report.model.Form3_1Report;

@Mapper(componentModel = "spring")
public interface Form3_1ReportMapper {
    Form3_1Report toEntity(Form3_1ReportRequest request);

    Form3_1ReportResponse toResponse(Form3_1Report entity);
}
