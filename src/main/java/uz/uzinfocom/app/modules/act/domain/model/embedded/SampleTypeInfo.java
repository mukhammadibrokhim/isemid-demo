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
public class SampleTypeInfo {

    @Column(name = "sample_type_id")
    private Integer sampleTypeId;

    @Column(name = "sample_type_uz")
    private String sampleTypeUz;

    @Column(name = "sample_type_ru")
    private String sampleTypeRu;
}
