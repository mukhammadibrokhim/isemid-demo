package uz.uzinfocom.app.modules.form058.application.command.create;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
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
    @Mapping(target = "senderOrganizationId", source = "senderOrganizationId")
    @Mapping(target = "receiverOrganizationId", source = "receiverOrganizationId")
    @Mapping(target = "hasLinkedCards", constant = "false")
    @Mapping(target = "deleted", constant = "false")

    @Mapping(target = "diagnosisInfo.mkb10Code", source = "mkb10Code")
    @Mapping(target = "diagnosisInfo.mkb10Name", source = "mkb10Name")

    @Mapping(target = "clinicalInfo.hospitalPlaceId", source = "hospitalPlaceId")

    @Mapping(target = "dateInfo.diseaseDate", source = "diseaseDate", qualifiedByName = "atStartOfDay")
    @Mapping(target = "dateInfo.firstVisitDate", source = "firstVisitDate", qualifiedByName = "atStartOfDay")
    @Mapping(target = "dateInfo.visitDate", source = "visitDate", qualifiedByName = "atStartOfDay")
    @Mapping(target = "dateInfo.initialReportDateTime", source = "initialReportDateTime")

    @Mapping(target = "epidemicInfo.diseasePlace", source = "diseasePlace")

    @Mapping(target = "reportInfo.notifierFullName", source = "notifierFullName")
    @Mapping(target = "reportInfo.journalFormCode", source = "journalFormCode")
    @Mapping(target = "reportInfo.comment", source = "comment")
    public abstract Form058 toEntity(CreateForm058Command command);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "status", source = "status")
    public abstract CreateForm058Result toResult(Form058 form058);

    @AfterMapping
    protected void attachLocation(
            CreateForm058Command command,
            @MappingTarget Form058 form058
    ) {
        if (!hasLocation(command)) {
            return;
        }

        form058.attachLocation(toLocation(command));
    }

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "latitude", source = "locationLatitude")
    @Mapping(target = "longitude", source = "locationLongitude")
    @Mapping(target = "location", source = "location")
    protected abstract Form058Location toLocation(CreateForm058Command command);

    private boolean hasLocation(CreateForm058Command command) {
        return command != null
                && (command.locationLatitude() != null
                || command.locationLongitude() != null
                || command.location() != null);
    }

    @Named("atStartOfDay")
    protected LocalDateTime atStartOfDay(LocalDate value) {
        return value == null ? null : value.atStartOfDay();
    }
}