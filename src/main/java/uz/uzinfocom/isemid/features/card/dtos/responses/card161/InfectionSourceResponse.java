package uz.uzinfocom.isemid.features.card.dtos.responses.card161;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InfectionSourceResponse {
    private Long id;
    private String fullName;
    private String diagnosisClinicalFormOrDonorStatus;
    private String contactInfoAndDonorResidence;
    private String testResult;}
