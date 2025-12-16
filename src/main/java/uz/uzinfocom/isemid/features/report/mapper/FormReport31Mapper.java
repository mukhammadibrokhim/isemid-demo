package uz.uzinfocom.isemid.features.report.mapper;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapperHelper;
import uz.uzinfocom.isemid.features.report.dtos.request.Form3_1ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.request.Form7ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.response.FormReport31Response;
import uz.uzinfocom.isemid.features.report.model.FormReport31;

@Mapper(componentModel = "spring", uses = {OrganizationMapperHelper.class})
public interface FormReport31Mapper {
    FormReport31 toEntity(Form3_1ReportRequest request);

    @Mapping(target = "createdOrg", source = "createdOrg.uuid", qualifiedByName = "toOrgMiniResponse")
    FormReport31Response toResponse(FormReport31 entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget FormReport31 entity, Form7ReportRequest request);
}
