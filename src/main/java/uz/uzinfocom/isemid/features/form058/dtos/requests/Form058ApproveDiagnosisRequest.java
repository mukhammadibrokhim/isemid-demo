package uz.uzinfocom.isemid.features.form058.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form058ApproveDiagnosisRequest {
    @NotNull(message = "finalMkb10Code is required")
    private String finalMkb10Code;
    @NotNull(message = "finalMkb10Name is required")
    private String finalMkb10Name;
    @NotNull(message = "approvedFullName is required")
    private String approvedFullName;
    @NotNull(message = "approvedOrgId is required")
    private UUID approvedOrgId;
}
