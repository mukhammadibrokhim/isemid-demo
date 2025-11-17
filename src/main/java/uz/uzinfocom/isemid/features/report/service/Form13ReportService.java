package uz.uzinfocom.isemid.features.report.service;

import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface Form13ReportService {
     Object generateForm13(User currentUser, LocalDate startDate, LocalDate endDate, String parentCode);
}
