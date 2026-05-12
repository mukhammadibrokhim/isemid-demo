package uz.uzinfocom.isemid.features.act.models.common;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResearchItemTypeInfo {
  private Integer researchTypeId;

  private Integer categoryId;
  private String categoryNameUz;
  private String categoryNameRu;

  private Integer itemTypeId;
  private String itemTypeNameUz;
  private String itemTypeNameRu;

}