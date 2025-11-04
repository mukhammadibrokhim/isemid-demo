package uz.uzinfocom.isemid.features.report.service;

import uz.uzinfocom.isemid.features.report.dtos.Form28_2ReportDto;

import java.time.LocalDate;
import java.util.List;

public interface Form28_2ReportService {
    List<Form28_2ReportDto> getReports(LocalDate fromDate, LocalDate toDate, String mkb10Code);
}
