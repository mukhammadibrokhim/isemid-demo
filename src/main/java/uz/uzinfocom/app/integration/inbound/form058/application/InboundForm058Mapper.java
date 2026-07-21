package uz.uzinfocom.app.integration.inbound.form058.application;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.integration.inbound.form058.web.InboundCreateForm058Request;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Command;
import uz.uzinfocom.app.modules.patient.web.mapper.PatientRequestMapper;
import uz.uzinfocom.app.platform.iam.application.shared.service.OrganizationMappingHelper;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class, uses = {OrganizationMappingHelper.class, PatientRequestMapper.class})
public interface InboundForm058Mapper {

    @Mapping(target = "source", source = "source")
    @Mapping(target = "senderOrganizationId", source = "senderOrganizationId")
    @Mapping(target = "receiverOrganizationId", source = "request.receiverOrganizationId", qualifiedByName = "activeOrganizationId")
    @Mapping(target = "hospitalPlaceId", source = "request.clinicalInfo.hospitalPlaceId", qualifiedByName = "nullableActiveOrganizationId")
    @Mapping(target = "diseasePlaceCode", source = "request.epidemicInfo.diseasePlaceCode")
    @Mapping(target = "diseaseCause", source = "request.epidemicInfo.diseaseCause")
    @Mapping(target = "epidemicMeasures", source = "request.epidemicInfo.epidemicMeasures")

    @Mapping(target = "notifierFullName", source = "request.reportInfo.notifierFullName")
    @Mapping(target = "journalFormCode", source = "request.reportInfo.journalFormCode")
    @Mapping(target = "comment", source = "request.reportInfo.comment")

    @Mapping(target = "locationLatitude", source = "request.location.latitude")
    @Mapping(target = "locationLongitude", source = "request.location.longitude")
    @Mapping(target = "location", source = "request.location.location")

    @Mapping(target = "diseaseDate", source = "request.dateInfo.diseaseDate")
    @Mapping(target = "firstVisitDate", source = "request.dateInfo.firstVisitDate")
    @Mapping(target = "visitDate", source = "request.dateInfo.visitDate")
    @Mapping(target = "admissionDate", source = "request.dateInfo.admissionDate")
    @Mapping(target = "diagnosisDate", source = "request.dateInfo.diagnosisDate")
    @Mapping(target = "initialReportDateTime", source = "request.dateInfo.initialReportDateTime")

    @Mapping(target = "patient", source = "request.patient")
    @Mapping(target = "finalMkb10Code", source = "request.diagnosisInfo.mkb10Code")
    @Mapping(target = "finalMkb10Name", source = "request.diagnosisInfo.mkb10Name")
    @Mapping(target = "mkb10Code", source = "request.diagnosisInfo.mkb10Code")
    @Mapping(target = "mkb10Name", source = "request.diagnosisInfo.mkb10Name")
    @Mapping(target = "mkb10UsageLimit", source = "request.diagnosisInfo.mkb10UsageLimit")
    @Mapping(target = "labConfirmation", source = "request.clinicalInfo.labConfirmation")
    CreateForm058Command toCommand(InboundCreateForm058Request request, String source, Long senderOrganizationId);
}
