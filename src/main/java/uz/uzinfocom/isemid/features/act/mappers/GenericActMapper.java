package uz.uzinfocom.isemid.features.act.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import uz.uzinfocom.isemid.features.act.models.Act;

public interface GenericActMapper {

  /**
   *
   * @param entity
   * Cleans up Institution fields after mapping by removing
   * values not applicable for the selected SubjectType.
   * Refer to normalize() method of Institution class
   */
  @AfterMapping
  default void normalizeInstitution(
      @MappingTarget Act entity) {
    if (entity.getInstitution() != null) {
      entity.getInstitution().normalize();
    }
  }

}
