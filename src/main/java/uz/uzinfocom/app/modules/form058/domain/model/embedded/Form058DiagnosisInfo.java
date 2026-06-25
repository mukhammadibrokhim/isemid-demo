package uz.uzinfocom.app.modules.form058.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Form058DiagnosisInfo {

    @Column(name = "mkb10_code", nullable = false, length = 20)
    private String mkb10Code;

    @Column(name = "mkb10_name", nullable = false, length = 512)
    private String mkb10Name;

    @Column(name = "final_mkb10_code", length = 20)
    private String finalMkb10Code;

    @Column(name = "final_mkb10_name", length = 512)
    private String finalMkb10Name;

    @Column(name = "mkb10_usage_limit")
    private Integer mkb10UsageLimit;
}