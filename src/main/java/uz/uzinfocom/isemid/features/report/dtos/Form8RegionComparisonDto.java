package uz.uzinfocom.isemid.features.report.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form8RegionComparisonDto {
    private Form8ReportDto firstPeriod;
    private Form8ReportDto secondPeriod;
    private Form8ReportDto difference;
}
