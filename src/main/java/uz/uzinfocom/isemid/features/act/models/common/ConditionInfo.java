package uz.uzinfocom.isemid.features.act.models.common;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConditionInfo {
  private Integer conditionId;

  @Embedded
  private LocalizedText description;

}
