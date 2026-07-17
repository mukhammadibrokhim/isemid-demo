package uz.uzinfocom.isemid.features.act.models.common;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Purpose {

  private Integer purposeId;
  private String samplingPurposeUz;
  private String samplingPurposeRu;
  private String samplingPurposeLoinc;

}