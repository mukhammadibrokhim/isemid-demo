package uz.uzinfocom.app.modules.act.domain.model.act223;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.modules.act.domain.model.Act;
import uz.uzinfocom.app.modules.act.domain.model.embedded.ConditionInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.EmployeeInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.PackageTypeInfo;
import uz.uzinfocom.app.modules.act.domain.model.embedded.Purpose;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "act223")
@NoArgsConstructor
@AllArgsConstructor
public class Act223 extends Act {

    @Column(name = "act_number")
    private Long actNumber;

    @Column(name = "supporting_documents_for_sampling")
    private String supportingDocumentsForSampling;

    @Column(name = "goal")
    private String goal;

    @Column(name = "activity_type_code")
    private String activityTypeCode;

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
    private Purpose purpose;

    @Column(name = "sample_taken_date_time")
    private LocalDateTime sampleTakenDateTime;

    @Column(name = "delivered_date_time")
    private LocalDateTime deliveredDateTime;

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

    @Column(name = "lis_organization_id")
    private Long lisOrganizationId;

    @Column(name = "laboratory_address")
    private String laboratoryAddress;

    @Embedded
    private PackageTypeInfo packageTypeInfo;

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    @OneToMany(mappedBy = "act223", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Act223Detail> act223Details = new ArrayList<>();
}
