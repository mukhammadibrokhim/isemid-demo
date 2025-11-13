package uz.uzinfocom.isemid.features.form058.dtos.requests;

import lombok.*;

import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form058ApproveDiagnosisRequest {
    private String finalMkb10Code;
    private String finalMkb10Name;
    private String approvedFullName;
    private UUID approvedOrgId;
}
