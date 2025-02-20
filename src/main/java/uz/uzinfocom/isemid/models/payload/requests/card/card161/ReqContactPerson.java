package uz.uzinfocom.isemid.models.payload.requests.card.card161;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqContactPerson {
    private Long id;
    private String contactFullName;
    private String contactAge;
    private String contactAddress;
    private String contactJobTypeAndLocation;
    private String ContactImmunizationStatus;
    private String contactRestrictionMeasures;
}
