package uz.uzinfocom.isemid.features.act.models.act154;

import static uz.uzinfocom.isemid.features.act.constants.ExternalSystemConstants.LIS_SANITARY_CONCLUSION_FIELD;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uz.uzinfocom.isemid.commons.exceptions.ProtocolNotReadyException;
import uz.uzinfocom.isemid.features.act.enums.ActStatus;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.act.models.LisDocumentAttachable;
import uz.uzinfocom.isemid.features.act.models.common.ConditionInfo;
import uz.uzinfocom.isemid.features.act.models.common.EmployeeInfo;
import uz.uzinfocom.isemid.features.act.models.common.PackageTypeInfo;
import uz.uzinfocom.isemid.features.act.models.common.Purpose;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Act154 extends Act implements LisDocumentAttachable {

   private String title;
   private Long actNumber;
   private String activityTypeCode;

   private LocalDateTime sampleTakenDateTime;
   private LocalDateTime deliveredDateTime;

   private String documentConfirmSampling;
   private String goal;

   @Embedded
   private Purpose purpose;

   @Embedded
   @AttributeOverrides({
       @AttributeOverride(name = "fullName", column = @Column(name = "sampler_full_name")),
       @AttributeOverride(name = "positionId", column = @Column(name = "sampler_position_id")),
       @AttributeOverride(name = "positionUz", column = @Column(name = "sampler_position_uz")),
       @AttributeOverride(name = "positionRu", column = @Column(name = "sampler_position_ru"))
   })
   private EmployeeInfo sampler;

   @Embedded
   @AttributeOverrides({
       @AttributeOverride(name = "fullName", column = @Column(name = "participant_full_name")),
       @AttributeOverride(name = "positionId", column = @Column(name = "participant_position_id")),
       @AttributeOverride(name = "positionUz", column = @Column(name = "participant_position_uz")),
       @AttributeOverride(name = "positionRu", column = @Column(name = "participant_position_ru"))
   })
   private EmployeeInfo participant;

   private String manufacturingCompany;
   private LocalDate manufactureDate;
   private String docNumberOfTakenObject;

   @Embedded
   @AttributeOverrides({
       @AttributeOverride(name = "conditionId", column = @Column(name = "special_condition_id")),
       @AttributeOverride(name = "description.uz", column = @Column(name = "special_sampling_conditions_uz", columnDefinition = "TEXT")),
       @AttributeOverride(name = "description.ru", column = @Column(name = "special_sampling_conditions_ru", columnDefinition = "TEXT"))
   })
   private ConditionInfo specialCondition;


   @Embedded
   @AttributeOverrides({
       @AttributeOverride(name = "conditionId", column = @Column(name = "storage_delivery_condition_id")),
       @AttributeOverride(name = "description.uz", column = @Column(name = "storage_delivery_conditions_uz", columnDefinition = "TEXT")),
       @AttributeOverride(name = "description.ru", column = @Column(name = "storage_delivery_conditions_ru", columnDefinition = "TEXT"))
   })
   private ConditionInfo storageAndDeliveryCondition;

   private Long lisOrganizationId;
   private String laboratoryAddress;

   @Embedded
   private PackageTypeInfo packageTypeInfo;


   @Column(columnDefinition = "TEXT")
   private String additionalInfo;

   @OneToMany(mappedBy = "act154", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
   @JsonManagedReference
   private Set<Act154Detail> act154Details = new HashSet<>();

   @JdbcTypeCode(SqlTypes.JSON)
   @Column(columnDefinition = "jsonb")
   private Map<String, Object> lisActResponse;

   @JdbcTypeCode(SqlTypes.JSON)
   @Column(columnDefinition = "jsonb")
   private Map<String, Object> lisProtocolResponse;

   @Override
   public void concludeProtocol(String message) {
      if (Objects.isNull(lisProtocolResponse)) {
         throw new ProtocolNotReadyException();
      }
      lisProtocolResponse.put(LIS_SANITARY_CONCLUSION_FIELD, message);
      setStatus(ActStatus.COMPLETED);
   }
}
