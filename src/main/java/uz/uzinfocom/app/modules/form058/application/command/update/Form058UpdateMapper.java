package uz.uzinfocom.app.modules.form058.application.command.update;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.app.modules.form058.domain.model.Form058;
import uz.uzinfocom.app.modules.form058.domain.model.Form058Location;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058ClinicalInfo;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058DateInfo;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058DiagnosisInfo;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058EpidemicInfo;
import uz.uzinfocom.app.modules.form058.domain.model.embedded.Form058ReportInfo;
import uz.uzinfocom.app.platform.mapping.CentralMapperConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper(config = CentralMapperConfig.class)
public interface Form058UpdateMapper {

    default void update(UpdateForm058Command command, @MappingTarget Form058 form058) {
        if (command == null || form058 == null) {
            return;
        }

        if (command.receiverOrganizationId() != null) {
            form058.setReceiverOrganizationId(command.receiverOrganizationId());
        }

        updateDiagnosisInfo(command, form058);
        updateClinicalInfo(command, form058);
        updateDateInfo(command, form058);
        updateEpidemicInfo(command, form058);
        updateReportInfo(command, form058);
        updateLocation(command, form058);
    }

    default UpdateForm058Result toResult(Form058 form058) {
        if (form058 == null) {
            return null;
        }

        return new UpdateForm058Result(
                form058.getId(),
                form058.getUuid(),
                form058.getStatus()
        );
    }

    private void updateDiagnosisInfo(UpdateForm058Command command, Form058 form058) {
        if (command.mkb10Code() == null && command.mkb10Name() == null) {
            return;
        }

        if (form058.getDiagnosisInfo() == null) {
            form058.setDiagnosisInfo(new Form058DiagnosisInfo());
        }
        if (command.mkb10Code() != null) {
            form058.getDiagnosisInfo().setMkb10Code(command.mkb10Code());
        }
        if (command.mkb10Name() != null) {
            form058.getDiagnosisInfo().setMkb10Name(command.mkb10Name());
        }
    }

    private void updateClinicalInfo(UpdateForm058Command command, Form058 form058) {
        if (command.hospitalPlaceId() == null) {
            return;
        }

        if (form058.getClinicalInfo() == null) {
            form058.setClinicalInfo(new Form058ClinicalInfo());
        }
        form058.getClinicalInfo().setHospitalPlaceId(command.hospitalPlaceId());
    }

    private void updateDateInfo(UpdateForm058Command command, Form058 form058) {
        if (command.diseaseDate() == null
                && command.firstVisitDate() == null
                && command.visitDate() == null
                && command.initialReportDateTime() == null) {
            return;
        }

        if (form058.getDateInfo() == null) {
            form058.setDateInfo(new Form058DateInfo());
        }
        if (command.diseaseDate() != null) {
            form058.getDateInfo().setDiseaseDate(atStartOfDay(command.diseaseDate()));
        }
        if (command.firstVisitDate() != null) {
            form058.getDateInfo().setFirstVisitDate(atStartOfDay(command.firstVisitDate()));
        }
        if (command.visitDate() != null) {
            form058.getDateInfo().setVisitDate(atStartOfDay(command.visitDate()));
        }
        if (command.initialReportDateTime() != null) {
            form058.getDateInfo().setInitialReportDateTime(command.initialReportDateTime());
        }
    }

    private void updateEpidemicInfo(UpdateForm058Command command, Form058 form058) {
        if (command.diseasePlace() == null) {
            return;
        }

        if (form058.getEpidemicInfo() == null) {
            form058.setEpidemicInfo(new Form058EpidemicInfo());
        }
        form058.getEpidemicInfo().setDiseasePlace(command.diseasePlace());
    }

    private void updateReportInfo(UpdateForm058Command command, Form058 form058) {
        if (command.notifierFullName() == null
                && command.journalFormCode() == null
                && command.comment() == null) {
            return;
        }

        if (form058.getReportInfo() == null) {
            form058.setReportInfo(new Form058ReportInfo());
        }
        if (command.notifierFullName() != null) {
            form058.getReportInfo().setNotifierFullName(command.notifierFullName());
        }
        if (command.journalFormCode() != null) {
            form058.getReportInfo().setJournalFormCode(command.journalFormCode());
        }
        if (command.comment() != null) {
            form058.getReportInfo().setComment(command.comment());
        }
    }

    private void updateLocation(UpdateForm058Command command, Form058 form058) {
        if (command.locationLatitude() == null
                && command.locationLongitude() == null
                && command.location() == null) {
            return;
        }

        if (form058.getLocation() == null) {
            form058.setLocation(new Form058Location());
        }
        if (command.locationLatitude() != null) {
            form058.getLocation().setLatitude(command.locationLatitude());
        }
        if (command.locationLongitude() != null) {
            form058.getLocation().setLongitude(command.locationLongitude());
        }
        if (command.location() != null) {
            form058.getLocation().setLocation(command.location());
        }
    }

    private LocalDateTime atStartOfDay(LocalDate value) {
        return value == null ? null : value.atStartOfDay();
    }
}
