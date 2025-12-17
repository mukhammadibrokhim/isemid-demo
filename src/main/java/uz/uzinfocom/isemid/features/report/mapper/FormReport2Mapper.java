package uz.uzinfocom.isemid.features.report.mapper;

import org.mapstruct.*;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapperHelper;
import uz.uzinfocom.isemid.features.report.dtos.request.Form2ReportRequest;
import uz.uzinfocom.isemid.features.report.dtos.response.FormReport2Response;
import uz.uzinfocom.isemid.features.report.model.FormReport2;

@Mapper(componentModel = "spring", uses = OrganizationMapperHelper.class)
public interface FormReport2Mapper {
    FormReport2 toEntity(Form2ReportRequest request);

    @Mapping(target = "createdOrg", source = "createdOrg.uuid", qualifiedByName = "toOrgMiniResponse")
    FormReport2Response toResponse(FormReport2 entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget FormReport2 entity, Form2ReportRequest request);
}
