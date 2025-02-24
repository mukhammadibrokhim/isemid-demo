package uz.uzinfocom.isemid.models.payload.requests.value_set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class ReqManualReport {
    private String code;
    private String shortName;
    private String nameUz;
    private String nameUzCyril;
    private String nameRu;
    private String nameKaa;
    private Boolean includeInTotal;
    private List<String> mkb10Codes;
}
