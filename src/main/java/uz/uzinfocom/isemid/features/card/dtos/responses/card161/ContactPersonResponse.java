package uz.uzinfocom.isemid.features.card.dtos.responses.card161;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactPersonResponse {
    private Long id;
    private String fullName;
    private String age;
    private String address;
    private String jobTypeAndLocation;
    private String immunizationStatus;
    private String restrictionMeasures;}
