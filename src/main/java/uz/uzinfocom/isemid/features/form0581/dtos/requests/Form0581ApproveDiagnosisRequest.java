package uz.uzinfocom.isemid.features.form0581.dtos.requests;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form0581ApproveDiagnosisRequest {
    private String finalMkb10Code;
    private String finalMkb10Name;
    private String approvedFullName;
    private UUID approvedOrgId;
}
