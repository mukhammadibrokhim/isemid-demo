package uz.uzinfocom.isemid.features.classifier.dtos.responses;

import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManualReportResponse {
    private String id;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String code;
    private String shortName;
    private String nameUz;
    private String nameUzCyril;
    private String nameRu;
    private String nameKaa;
    private Boolean includeInTotal;
    private List<Mkb10Response> mkb10Codes;
}
