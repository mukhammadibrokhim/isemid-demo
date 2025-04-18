package uz.uzinfocom.isemid.features.report.service;

import uz.uzinfocom.isemid.commons.dtos.ApiResponse;
import uz.uzinfocom.isemid.features.patient.enums.Gender;
import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface Form1ReportService {
    ApiResponse<?> getReport(LocalDate fromDate, LocalDate toDate, LocalDate birthDate, Gender gender, User currentUser);
}
