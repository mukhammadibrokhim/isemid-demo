package uz.uzinfocom.isemid.features.report.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form28_2ReportDto {
    private String shortName;
    private String titleUz;
    private String titleUzCyril;
    private String titleRu;
    private String titleKaa;
    private String code;
    private Long totalCount;
    private Long age0to17Count;
    private Long under1MonthCount;      // 1 ойгача / до 1 месяца
    private Long age1MonthTo1YearCount; // 1 ойдан 1 ёшгача / от 1 месяца до 1 года
}
