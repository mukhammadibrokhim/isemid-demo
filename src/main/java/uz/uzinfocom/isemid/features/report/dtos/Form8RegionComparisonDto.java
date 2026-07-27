package uz.uzinfocom.isemid.features.report.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form8RegionComparisonDto {
    private String stateCode;
    private Form8ReportDto firstPeriod;
    private Form8ReportDto secondPeriod;
    private Form8ReportDto difference;
    private List<Form8CityComparisonDto> cities;
}
