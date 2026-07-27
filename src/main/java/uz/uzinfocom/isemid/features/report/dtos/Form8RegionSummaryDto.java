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
public class Form8RegionSummaryDto {
    private String stateCode;
    private Form8ReportDto statistics;
    private List<Form8ReportDto> cities;
}
