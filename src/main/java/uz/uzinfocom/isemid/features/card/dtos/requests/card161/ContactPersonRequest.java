package uz.uzinfocom.isemid.features.card.dtos.requests.card161;

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
public class ContactPersonRequest {
    private Long id;
    private String fullName;
    private String age;
    private String address;
    private String jobTypeAndLocation;
    private String immunizationStatus;
    private String restrictionMeasures;
}
