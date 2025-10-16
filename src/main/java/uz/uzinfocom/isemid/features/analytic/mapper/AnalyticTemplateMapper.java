package uz.uzinfocom.isemid.features.analytic.mapper;

import org.mapstruct.Mapper;
import uz.uzinfocom.isemid.features.analytic.dto.request.AnalyticTemplateRequest;
import uz.uzinfocom.isemid.features.analytic.dto.response.AnalyticTemplateResponse;
import uz.uzinfocom.isemid.features.analytic.model.AnalyticTemplate;

@Mapper(componentModel = "spring")
public interface AnalyticTemplateMapper {
    AnalyticTemplate toEntity(AnalyticTemplateRequest request);

    AnalyticTemplateResponse toResponse(AnalyticTemplate entity);
}
