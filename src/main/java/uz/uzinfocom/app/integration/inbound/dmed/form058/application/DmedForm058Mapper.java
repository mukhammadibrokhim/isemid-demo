package uz.uzinfocom.app.integration.inbound.dmed.form058.application;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.integration.inbound.dmed.form058.web.DmedCreateForm058Request;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Command;
import uz.uzinfocom.app.modules.patient.web.mapper.PatientRequestMapper;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationMappingHelper;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class, uses = {OrganizationMappingHelper.class, PatientRequestMapper.class})
public interface DmedForm058Mapper {

    @Mapping(target = "source", source = "source")
    @Mapping(target = "senderOrganizationId", source = "senderOrganizationId")
    @Mapping(target = "receiverOrganizationId", source = "request.receiverOrganizationId", qualifiedByName = "activeOrganizationId")
    @Mapping(target = "hospitalPlaceId", source = "request.hospitalPlaceId", qualifiedByName = "nullableActiveOrganizationId")
    @Mapping(target = "diseasePlaceCode", source = "request.diseasePlaceCode")
    @Mapping(target = "diseaseCause", source = "request.diseaseCause")
    @Mapping(target = "epidemicMeasures", source = "request.epidemicMeasures")

    @Mapping(target = "notifierFullName", source = "request.notifierFullName")
    @Mapping(target = "journalFormCode", source = "request.journalFormCode")
    @Mapping(target = "comment", source = "request.comment")

    @Mapping(target = "locationLatitude", source = "request.location.latitude")
    @Mapping(target = "locationLongitude", source = "request.location.longitude")
    @Mapping(target = "location", source = "request.location.location")

    @Mapping(target = "diseaseDate", source = "request.diseaseDate")
    @Mapping(target = "firstVisitDate", source = "request.firstVisitDate")
    @Mapping(target = "visitDate", source = "request.visitDate")
    @Mapping(target = "admissionDate", source = "request.admissionDate")
    @Mapping(target = "diagnosisDate", source = "request.diagnosisDate")
    @Mapping(target = "initialReportDateTime", source = "request.initialReportDateTime")

    @Mapping(target = "patient", source = "request.patient")
    @Mapping(target = "finalMkb10Code", source = "request.finalMkb10Code")
    @Mapping(target = "finalMkb10Name", source = "request.finalMkb10Name")
    @Mapping(target = "mkb10Code", source = "request.mkb10Code")
    @Mapping(target = "mkb10Name", source = "request.mkb10Name")
    @Mapping(target = "mkb10UsageLimit", source = "request.mkb10UsageLimit")
    @Mapping(target = "labConfirmation", source = "request.labConfirmation")
    CreateForm058Command toCommand(DmedCreateForm058Request request, String source, Long senderOrganizationId);
}
