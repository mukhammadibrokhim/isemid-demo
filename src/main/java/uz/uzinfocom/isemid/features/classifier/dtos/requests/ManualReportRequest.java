package uz.uzinfocom.isemid.features.classifier.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManualReportRequest {
    private String code;
    private String shortName;
    private String nameUz;
    private String nameUzCyril;
    private String nameRu;
    private String nameKaa;
    private Boolean includeInTotal;
    private Set<String> mkb10Codes;
}
