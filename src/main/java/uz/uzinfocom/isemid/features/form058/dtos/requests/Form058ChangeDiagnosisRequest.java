package uz.uzinfocom.isemid.features.form058.dtos.requests;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form058ChangeDiagnosisRequest {
    private String finalMkb10Code;
    private String finalMkb10Name;
}
