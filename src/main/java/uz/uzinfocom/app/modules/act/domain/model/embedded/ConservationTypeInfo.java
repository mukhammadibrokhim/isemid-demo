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
public class ConservationTypeInfo {

    @Column(name = "conservation_method_id")
    private Integer conservationMethodId;

    @Column(name = "conservation_methods_uz")
    private String conservationMethodsUz;

    @Column(name = "conservation_methods_ru")
    private String conservationMethodsRu;
}
