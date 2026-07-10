package uz.uzinfocom.app.modules.form0581.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Form0581DiagnosisInfo {

    @Column(name = "mkb10_code", nullable = false, length = 20)
    private String mkb10Code;

    @Column(name = "mkb10_name", nullable = false, length = 512)
    private String mkb10Name;

    @Column(name = "injury_localization", length = 500)
    private String injuryLocalization;

    @Column(name = "final_mkb10_code", length = 20)
    private String finalMkb10Code;

    @Column(name = "final_mkb10_name", length = 512)
    private String finalMkb10Name;
}
