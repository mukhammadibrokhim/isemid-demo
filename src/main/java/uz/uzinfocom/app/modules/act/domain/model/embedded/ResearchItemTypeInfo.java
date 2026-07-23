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
public class ResearchItemTypeInfo {

    @Column(name = "research_type_id")
    private Integer researchTypeId;

    @Column(name = "research_type_name_uz")
    private String researchTypeNameUz;

    @Column(name = "research_type_name_ru")
    private String researchTypeNameRu;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "category_name_uz")
    private String categoryNameUz;

    @Column(name = "category_name_ru")
    private String categoryNameRu;

    @Column(name = "item_type_id")
    private Integer itemTypeId;

    @Column(name = "item_type_name_uz")
    private String itemTypeNameUz;

    @Column(name = "item_type_name_ru")
    private String itemTypeNameRu;
}
