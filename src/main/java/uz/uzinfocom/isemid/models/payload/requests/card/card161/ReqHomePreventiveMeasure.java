package uz.uzinfocom.isemid.models.payload.requests.card.card161;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqHomePreventiveMeasure {
    private String notifiedPerson;
    private String nextDiseasePrevention; //DMED integration
    private String laboratoryExamination; //DMED integration
    private String observationResult;
}
