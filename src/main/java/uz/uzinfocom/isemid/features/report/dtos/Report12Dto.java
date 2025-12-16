package uz.uzinfocom.isemid.features.report.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Report12Dto {
    private Long id;
    private String regionName;
    private Integer casesAtPeriodStart;
    private Integer registeredTotal;
    private Integer registeredChildren;
    private Integer registeredAdults;
    private Integer registeredWomen;
    private Integer registeredUrban;
    private Integer registeredRural;
    private Integer completedExamination;
    private Integer needExamination;
    private Integer confirmedDiagnosis;
    private Integer hospitalized;
    private Integer casesAtPeriodEnd;
    private Integer growthRate;
}
