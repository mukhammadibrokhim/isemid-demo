package uz.uzinfocom.isemid.features.report.service;

import uz.uzinfocom.isemid.features.user.models.User;

import java.time.LocalDate;

public interface Form11ReportService {
    Object generate(LocalDate startDate, LocalDate endDate, String parentCode, Long koef, Long population, User user);
}
