package uz.uzinfocom.isemid.models.payload.requests.card.card161;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqInfectionSourceDetail {
    private String infectionSourceNotFoundCode;
    private String personFullName;
    private String infectionSourceDiseasePeriodCode;
    private String animalTypeCode;
}
