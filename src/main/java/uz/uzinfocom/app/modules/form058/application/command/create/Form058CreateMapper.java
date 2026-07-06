package uz.uzinfocom.app.modules.form058.application.command.create;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.domain.model.Form058Location;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(config = CentralMapperConfig.class)
public abstract class Form058CreateMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "status", constant = "SENT")
    @Mapping(target = "source", source = "source")
    @Mapping(target = "patient", ignore = true)

    @Mapping(target = "senderOrganizationId", source = "senderOrganizationId")
    @Mapping(target = "receiverOrganizationId", source = "receiverOrganizationId")

    @Mapping(target = "diagnosisInfo.mkb10Code", source = "mkb10Code")
    @Mapping(target = "diagnosisInfo.mkb10Name", source = "mkb10Name")
    @Mapping(target = "diagnosisInfo.finalMkb10Code", expression = "java(createForm058Command.resolvedFinalMkb10Code())")
    @Mapping(target = "diagnosisInfo.finalMkb10Name", expression = "java(createForm058Command.resolvedFinalMkb10Name())")
    @Mapping(target = "diagnosisInfo.mkb10UsageLimit", source = "mkb10UsageLimit")

    @Mapping(target = "clinicalInfo.labConfirmation", source = "labConfirmation")
    @Mapping(target = "clinicalInfo.hospitalPlaceId", source = "hospitalPlaceId")

    @Mapping(target = "dateInfo.diseaseDate", source = "diseaseDate", qualifiedByName = "atStartOfDay")
    @Mapping(target = "dateInfo.firstVisitDate", source = "firstVisitDate", qualifiedByName = "atStartOfDay")
    @Mapping(target = "dateInfo.visitDate", source = "visitDate", qualifiedByName = "atStartOfDay")
    @Mapping(target = "dateInfo.admissionDate", source = "admissionDate", qualifiedByName = "atStartOfDay")
    @Mapping(target = "dateInfo.diagnosisDate", source = "diagnosisDate", qualifiedByName = "atStartOfDay")
    @Mapping(target = "dateInfo.initialReportDateTime", source = "initialReportDateTime")

    @Mapping(target = "epidemicInfo.diseasePlaceCode", source = "diseasePlaceCode")
    @Mapping(target = "epidemicInfo.diseaseCause", source = "diseaseCause")
    @Mapping(target = "epidemicInfo.epidemicMeasures", source = "epidemicMeasures")

    @Mapping(target = "reportInfo.notifierFullName", source = "notifierFullName")
    @Mapping(target = "reportInfo.journalFormCode", source = "journalFormCode")
    @Mapping(target = "reportInfo.comment", source = "comment")

    @Mapping(target = "location", source = ".", qualifiedByName = "toLocation")

    @Mapping(target = "hasLinkedCards", constant = "false")
    @Mapping(target = "assignedCardId", ignore = true)
    @Mapping(target = "cancellationInfo", ignore = true)
    @Mapping(target = "approvalInfo", ignore = true)
    @Mapping(target = "deleteInfo", ignore = true)
    public abstract Form058 toEntity(CreateForm058Command command);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "status", source = "status")
    public abstract CreateForm058Result toResult(Form058 form058);

    @Named("toLocation")
    protected Form058Location toLocation(CreateForm058Command command) {
        if (!hasLocation(command)) {
            return null;
        }

        return Form058Location.builder()
                .latitude(command.locationLatitude())
                .longitude(command.locationLongitude())
                .location(command.location())
                .build();
    }

    private boolean hasLocation(CreateForm058Command command) {
        return command != null
                && (
                command.locationLatitude() != null
                        || command.locationLongitude() != null
                        || hasText(command.location())
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Named("atStartOfDay")
    protected LocalDateTime atStartOfDay(LocalDate value) {
        return value == null ? null : value.atStartOfDay();
    }
}