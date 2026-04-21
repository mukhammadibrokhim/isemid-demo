package uz.uzinfocom.isemid.features.act.models.act154;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import uz.uzinfocom.isemid.features.act.models.Act;

import java.util.HashSet;
import java.util.Set;
import uz.uzinfocom.isemid.features.act.models.common.ConditionInfo;
import uz.uzinfocom.isemid.features.act.models.common.EmployeeInfo;
import uz.uzinfocom.isemid.features.act.models.common.Institution;
import uz.uzinfocom.isemid.features.act.models.common.PackageTypeInfo;
import uz.uzinfocom.isemid.features.act.models.common.Purpose;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Act154 extends Act {

   @Embedded
   private Institution institution;

   private String title;
   private Long serialNumber;
   private LocalDateTime sampleTakenDateTime;
   private LocalDateTime deliveredDateTime;

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


   @Embedded
   @AttributeOverrides({
       @AttributeOverride(name = "conditionId", column = @Column(name = "special_condition_id")),
       @AttributeOverride(name = "description.uz", column = @Column(name = "special_sampling_conditions_uz", columnDefinition = "TEXT")),
       @AttributeOverride(name = "description.ru", column = @Column(name = "special_sampling_conditions_ru", columnDefinition = "TEXT"))
   })
   private ConditionInfo specialCondition;

   @Embedded
   @AttributeOverrides({
       @AttributeOverride(name = "conditionId", column = @Column(name = "delivery_condition_id")),
       @AttributeOverride(name = "description.uz", column = @Column(name = "delivery_conditions_uz", columnDefinition = "TEXT")),
       @AttributeOverride(name = "description.ru", column = @Column(name = "delivery_conditions_ru", columnDefinition = "TEXT"))
   })
   private ConditionInfo deliveryCondition;

   @Embedded
   @AttributeOverrides({
       @AttributeOverride(name = "conditionId", column = @Column(name = "storage_condition_id")),
       @AttributeOverride(name = "description.uz", column = @Column(name = "storage_conditions_uz", columnDefinition = "TEXT")),
       @AttributeOverride(name = "description.ru", column = @Column(name = "storage_conditions_ru", columnDefinition = "TEXT"))
   })
   private ConditionInfo storageCondition;

   @Embedded
   private PackageTypeInfo packageTypeInfo;


   @Column(columnDefinition = "TEXT")
   private String additionalInfo;

   @OneToMany(mappedBy = "act154", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
   @JsonManagedReference
   private Set<Act154Detail> act154Details = new HashSet<>();
}
