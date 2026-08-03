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

    @Column(name = "icd10_code", nullable = false, length = 20)
    private String icd10Code;

    @Column(name = "icd10_name", nullable = false, length = 512)
    private String icd10Name;

    @Column(name = "final_icd10_code", length = 20)
    private String finalIcd10Code;

    @Column(name = "final_icd10_name", length = 512)
    private String finalIcd10Name;

    @Column(name = "icd10_usage_limit")
    private Integer icd10UsageLimit;
}