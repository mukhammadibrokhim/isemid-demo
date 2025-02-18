package uz.uzinfocom.isemid.models.payload.requests.card.card161;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqContactPerson {
    private String fullName;
    private String age;
    private String address;
    private String jobTypeAndLocation;
    private String immunizationStatus;
    private String restrictionMeasures;
}
