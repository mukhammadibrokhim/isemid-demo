package uz.uzinfocom.isemid.features.act.models.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.isemid.features.act.enums.SubjectType;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Institution {

  /**
   * LEGAL_ENTITY / GEOGRAPHIC_POINT / PHYSICAL_PERSON
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "subject_type", length = 50)
  private SubjectType subjectType;

  private Integer tin;

  private String institutionName;

  private String institutionAddress;

  private String institutionLegalAddress;
}