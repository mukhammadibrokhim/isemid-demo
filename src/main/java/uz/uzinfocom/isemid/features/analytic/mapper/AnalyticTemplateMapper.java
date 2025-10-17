package uz.uzinfocom.isemid.features.analytic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.isemid.features.analytic.dto.request.AnalyticTemplateRequest;
import uz.uzinfocom.isemid.features.analytic.dto.response.AnalyticTemplateResponse;
import uz.uzinfocom.isemid.features.analytic.model.AnalyticTemplate;

@Mapper(componentModel = "spring")
public interface AnalyticTemplateMapper {
    @Mapping(target = "name", source = "name")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "owner", ignore = true)
    AnalyticTemplate toEntity(AnalyticTemplateRequest request);

    AnalyticTemplateResponse toResponse(AnalyticTemplate entity);
}
