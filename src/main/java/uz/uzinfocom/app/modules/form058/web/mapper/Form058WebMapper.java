package uz.uzinfocom.app.modules.form058.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uz.uzinfocom.app.modules.form058.application.command.approve.ApproveForm058Command;
import uz.uzinfocom.app.modules.form058.application.command.cancel.CancelForm058Command;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Command;
import uz.uzinfocom.app.modules.form058.application.command.create.CreateForm058Result;
import uz.uzinfocom.app.modules.form058.application.command.update.UpdateForm058Command;
import uz.uzinfocom.app.modules.form058.application.command.update.UpdateForm058Result;
import uz.uzinfocom.app.modules.form058.application.query.dto.Form058DetailResult;
import uz.uzinfocom.app.modules.form058.application.query.dto.Form058TableResult;
import uz.uzinfocom.app.modules.form058.application.shared.OrganizationMappingHelper;
import uz.uzinfocom.app.modules.form058.web.request.ApproveForm058Request;
import uz.uzinfocom.app.modules.form058.web.request.CancelForm058Request;
import uz.uzinfocom.app.modules.form058.web.request.CreateForm058Request;
import uz.uzinfocom.app.modules.form058.web.request.UpdateForm058Request;
import uz.uzinfocom.app.modules.form058.web.response.CreateForm058Response;
import uz.uzinfocom.app.modules.form058.web.response.Form058DetailedResponse;
import uz.uzinfocom.app.modules.form058.web.response.Form058TableResponse;
import uz.uzinfocom.app.modules.form058.web.response.UpdateForm058Response;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

@Mapper(config = CentralMapperConfig.class, uses = OrganizationMappingHelper.class)
public interface Form058WebMapper {

    @Mapping(target = "senderOrganizationId", source = "senderOrganizationId", qualifiedByName = "activeOrganizationId")
    @Mapping(target = "receiverOrganizationId", source = "receiverOrganizationId", qualifiedByName = "activeOrganizationId")
    @Mapping(target = "patientNnuzb", source = "patient.nnuzb")
    @Mapping(target = "patientPinfl", source = "patient.pinfl")
    @Mapping(target = "patientFullName", source = "patient.fullName")
    @Mapping(target = "patientBirthDate", source = "patient.birthDate")
    @Mapping(target = "patientGender", source = "patient.gender")
    @Mapping(target = "patientPhone", source = "patient.phone")
    @Mapping(target = "locationRegionCode", source = "location.regionCode")
    @Mapping(target = "locationDistrictCode", source = "location.districtCode")
    @Mapping(target = "locationNeighborhoodCode", source = "location.neighborhoodCode")
    @Mapping(target = "locationAddress", source = "location.address")
    CreateForm058Command toCommand(CreateForm058Request request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "receiverOrganizationId", source = "request.receiverOrganizationId", qualifiedByName = "activeOrganizationId")
    @Mapping(target = "mkb10Code", source = "request.mkb10Code")
    @Mapping(target = "mkb10Name", source = "request.mkb10Name")
    @Mapping(target = "diseaseDate", source = "request.diseaseDate")
    @Mapping(target = "firstVisitDate", source = "request.firstVisitDate")
    @Mapping(target = "visitDate", source = "request.visitDate")
    @Mapping(target = "initialReportDateTime", source = "request.initialReportDateTime")
    @Mapping(target = "hospitalPlaceId", source = "request.hospitalPlaceId")
    @Mapping(target = "diseasePlace", source = "request.diseasePlace")
    @Mapping(target = "notifierFullName", source = "request.notifierFullName")
    @Mapping(target = "journalFormCode", source = "request.journalFormCode")
    @Mapping(target = "comment", source = "request.comment")
    @Mapping(target = "patientNnuzb", source = "request.patient.nnuzb")
    @Mapping(target = "patientPinfl", source = "request.patient.pinfl")
    @Mapping(target = "patientFullName", source = "request.patient.fullName")
    @Mapping(target = "patientBirthDate", source = "request.patient.birthDate")
    @Mapping(target = "patientGender", source = "request.patient.gender")
    @Mapping(target = "patientPhone", source = "request.patient.phone")
    @Mapping(target = "locationRegionCode", source = "request.location.regionCode")
    @Mapping(target = "locationDistrictCode", source = "request.location.districtCode")
    @Mapping(target = "locationNeighborhoodCode", source = "request.location.neighborhoodCode")
    @Mapping(target = "locationAddress", source = "request.location.address")
    UpdateForm058Command toCommand(Long id, UpdateForm058Request request);

    CancelForm058Command toCommand(Long formId, CancelForm058Request request);

    ApproveForm058Command toCommand(Long formId, ApproveForm058Request request);

    CreateForm058Response toResponse(CreateForm058Result result);

    UpdateForm058Response toResponse(UpdateForm058Result result);

    Form058TableResponse toResponse(Form058TableResult result);

    Form058DetailedResponse toResponse(Form058DetailResult result);
}
