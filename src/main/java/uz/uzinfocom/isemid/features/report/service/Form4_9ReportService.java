package uz.uzinfocom.isemid.features.report.service;

import uz.uzinfocom.isemid.features.patient.enums.Gender;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface Form4_9ReportService {
    Object getReport(
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate birthDate,
            String mkb10Code,
            Gender gender,
            User currentUser,
            String parentCode
    );
}
