package uz.uzinfocom.isemid.features.report.service;

import uz.uzinfocom.isemid.features.report.dtos.Form28_1ReportDto;

import java.time.LocalDate;
import java.util.List;

public interface Form28_1ReportService {
    List<Form28_1ReportDto> getReports(LocalDate fromDate, LocalDate toDate, String mkb10Code);
}
