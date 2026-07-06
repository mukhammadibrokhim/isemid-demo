package uz.uzinfocom.app.modules.form058.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Command;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Result;
import uz.uzinfocom.app.modules.form058.application.command.update.UpdateForm058Command;
import uz.uzinfocom.app.modules.form058.application.command.update.UpdateForm058Result;
import uz.uzinfocom.app.modules.form058.application.shared.OrganizationMappingHelper;
import uz.uzinfocom.app.modules.form058.web.request.CreateForm058Request;
import uz.uzinfocom.app.modules.form058.web.request.UpdateForm058Request;
import uz.uzinfocom.app.modules.form058.web.response.CreateForm058Response;
import uz.uzinfocom.app.modules.form058.web.response.UpdateForm058Response;
import uz.uzinfocom.app.modules.patient.web.mapper.PatientRequestMapper;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class, uses = {OrganizationMappingHelper.class, PatientRequestMapper.class})
public interface Form058WebMapper {

    @Mapping(target = "source", source = "source")
    @Mapping(target = "senderOrganizationId", source = "request.senderOrganizationId", qualifiedByName = "activeOrganizationId")
    @Mapping(target = "receiverOrganizationId", source = "request.receiverOrganizationId", qualifiedByName = "activeOrganizationId")
    @Mapping(target = "hospitalPlaceId", source = "request.hospitalPlaceId", qualifiedByName = "activeOrganizationId")
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
    @Mapping(target = "finalMkb10Code", source = "request.mkb10Code")
    @Mapping(target = "finalMkb10Name", source = "request.mkb10Name")
    @Mapping(target = "mkb10UsageLimit", source = "request.mkb10UsageLimit")
    @Mapping(target = "labConfirmation", source = "request.labConfirmation")
    CreateForm058Command toCommand(CreateForm058Request request, String source);


    @Mapping(target = "id", source = "id")
    @Mapping(target = "receiverOrganizationId", source = "request.receiverOrganizationId", qualifiedByName = "nullableActiveOrganizationId")
    @Mapping(target = "mkb10Code", source = "request.mkb10Code")
    @Mapping(target = "mkb10Name", source = "request.mkb10Name")
    @Mapping(target = "diseaseDate", source = "request.diseaseDate")
    @Mapping(target = "firstVisitDate", source = "request.firstVisitDate")
    @Mapping(target = "visitDate", source = "request.visitDate")
    @Mapping(target = "initialReportDateTime", source = "request.initialReportDateTime")
    @Mapping(target = "hospitalPlaceId", source = "request.hospitalPlaceId")
    @Mapping(target = "diseasePlaceCode", source = "request.diseasePlaceCode")
    @Mapping(target = "notifierFullName", source = "request.notifierFullName")
    @Mapping(target = "journalFormCode", source = "request.journalFormCode")
    @Mapping(target = "comment", source = "request.comment")
    @Mapping(target = "patient", source = "request.patient")
    @Mapping(target = "locationLatitude", source = "request.location.latitude")
    @Mapping(target = "locationLongitude", source = "request.location.longitude")
    @Mapping(target = "location", source = "request.location.location")
    UpdateForm058Command toCommand(Long id, UpdateForm058Request request);

    CreateForm058Response toResponse(CreateForm058Result result);

    UpdateForm058Response toResponse(UpdateForm058Result result);

}