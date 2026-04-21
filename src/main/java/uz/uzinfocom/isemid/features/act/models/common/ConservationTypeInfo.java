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
public class ConservationTypeInfo {

  private Integer conservationMethodId;
  private String conservationMethodsUz;
  private String conservationMethodsRu;

}