package uz.uzinfocom.isemid.features.report.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form12ReportDto {
    private String shortName;
    private String titleUz;
    private String titleUzCyril;
    private String titleRu;
    private String titleKaa;
    private String code;
    private Long totalCount;
    private Long below14Count;
    private Long below18Count;
}
