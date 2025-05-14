package uz.uzinfocom.isemid.features.form058.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form058ChangeDiagnosisRequest {
    @NotBlank
    private String finalMkb10Code;
    @NotBlank
    private String finalMkb10Name;
}
