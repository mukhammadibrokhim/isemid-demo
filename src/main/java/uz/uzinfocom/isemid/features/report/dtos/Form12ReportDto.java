package uz.uzinfocom.isemid.features.report.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form12ReportDto {
    private String titleUz;
    private String titleUzCyril;
    private String titleRu;
    private String titleKaa;
    private String mkb10Codes;
    private String code;
    private Long totalCount;
    private Long below18Count;
}
