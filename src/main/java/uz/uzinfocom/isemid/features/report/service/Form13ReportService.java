package uz.uzinfocom.isemid.features.report.service;

import uz.uzinfocom.isemid.features.report.dtos.Form13ReportDto;

import java.time.LocalDate;

public interface Form13ReportService {
     Form13ReportDto generateForm13(LocalDate start, LocalDate end);
}
