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
public class PackageTypeInfo {

    @Column(name = "package_type_id")
    private Integer packageTypeId;

    @Column(name = "package_type_uz")
    private String packageTypeUz;

    @Column(name = "package_type_ru")
    private String packageTypeRu;
}
