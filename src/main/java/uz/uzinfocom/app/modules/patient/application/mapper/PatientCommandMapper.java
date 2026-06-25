package uz.uzinfocom.app.modules.patient.application.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.patient.application.command.CreatePatientCommand;
import uz.uzinfocom.app.modules.patient.domain.model.Patient;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class, uses = PatientCommandMappingHelper.class)
public interface PatientCommandMapper {

    @BeanMapping(qualifiedByName = "linkPatientChildren")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdOrg", ignore = true)
    @Mapping(target = "createdOrgUuid", ignore = true)
    @Mapping(target = "updatedOrg", ignore = true)
    @Mapping(target = "updatedOrgUuid", ignore = true)
    @Mapping(target = "ageYears", ignore = true)
    @Mapping(target = "ageMonths", ignore = true)
    @Mapping(target = "identifiers", source = "identifiers", qualifiedByName = "toPatientIdentifiers")
    @Mapping(target = "addresses", source = "addresses", qualifiedByName = "toPatientAddresses")
    @Mapping(target = "affiliations", source = "affiliations", qualifiedByName = "toPatientAffiliations")
    Patient toEntity(CreatePatientCommand command);
}
