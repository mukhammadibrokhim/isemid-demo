package uz.uzinfocom.isemid.features.report.service;

import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface Form9ReportService {
    Object generateFor9(User currentUser, LocalDate startDate, LocalDate endDate, String parentCode);
}
