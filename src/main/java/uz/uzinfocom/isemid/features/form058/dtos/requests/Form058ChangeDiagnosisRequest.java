package uz.uzinfocom.isemid.features.form058.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form058ChangeDiagnosisRequest {
    @NotBlank(message = "{validation.form058.finalMkb10Code.notBlank}")
    private String finalMkb10Code;
    @NotBlank(message = "{validation.form058.finalMkb10Name.notBlank}")
    private String finalMkb10Name;
}
