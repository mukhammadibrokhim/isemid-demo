package uz.uzinfocom.isemid.features.card.dtos.responses.card161;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScreenedGroupResponse {
    private Long id;
    private String teamName;
    private String prophylacticAddress;
    private String contactCount;
    private String requiredProphylacticSubstance;
    private String treatedWithProphylacticSubstance;
    private String laboratoryTestConducted;}
