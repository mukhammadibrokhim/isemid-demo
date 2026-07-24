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
public class PackageTypeInfo {
  private Integer packageTypeId;
  private String packageTypeUz;
  private String packageTypeRu;

}