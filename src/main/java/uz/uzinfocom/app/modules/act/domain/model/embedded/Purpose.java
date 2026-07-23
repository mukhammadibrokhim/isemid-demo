package uz.uzinfocom.app.modules.act.domain.model.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Purpose {

    @Column(name = "purpose_id")
    private Integer purposeId;

    @Column(name = "sampling_purpose_uz")
    private String samplingPurposeUz;

    @Column(name = "sampling_purpose_ru")
    private String samplingPurposeRu;

    @Column(name = "sampling_purpose_loinc")
    private String samplingPurposeLoinc;
}
