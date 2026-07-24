package uz.uzinfocom.isemid.features.form129.mappers;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import uz.uzinfocom.isemid.features.form129.dtos.requests.Form129Request;
import uz.uzinfocom.isemid.features.form129.dtos.responses.Form129Response;
import uz.uzinfocom.isemid.features.form129.models.Form129;
import uz.uzinfocom.isemid.features.organization.mappers.OrganizationMapperHelper;
import uz.uzinfocom.isemid.features.patient.mappers.PatientMapper;

@Mapper(componentModel = "spring", uses = {
        PatientMapper.class,
        OrganizationMapperHelper.class,
        LabTestResultMapper.class
})
public interface Form129Mapper {

    @Mapping(source = "senderOrganizationId", target = "senderOrganizationId", qualifiedByName = "uuidToLongId")
    @Mapping(source = "receiverOrganizationId", target = "receiverOrganizationId", qualifiedByName = "uuidToLongId")
    Form129 toEntity(Form129Request request);

    @Mapping(target = "senderOrganization", source = "senderOrganizationId", qualifiedByName = "toOrgMiniResponse")
    @Mapping(target = "receiverOrganization", source = "receiverOrganizationId", qualifiedByName = "toOrgMiniResponse")
    Form129Response toResponse(Form129 entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "senderOrganizationId", target = "senderOrganizationId", qualifiedByName = "uuidToLongId")
    @Mapping(source = "receiverOrganizationId", target = "receiverOrganizationId", qualifiedByName = "uuidToLongId")
    void update(@MappingTarget Form129 existing, Form129Request request);
}
