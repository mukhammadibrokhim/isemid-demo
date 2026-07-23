package uz.uzinfocom.app.modules.act.domain.model.act154;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "act154")
@NoArgsConstructor
@AllArgsConstructor
public class Act154 extends Act {

    @Column(name = "title")
    private String title;

    @Column(name = "act_number")
    private Long actNumber;

    @Column(name = "activity_type_code")
    private String activityTypeCode;

    @Column(name = "sample_taken_date_time")
    private LocalDateTime sampleTakenDateTime;

    @Column(name = "delivered_date_time")
    private LocalDateTime deliveredDateTime;

    @Column(name = "document_confirm_sampling")
    private String documentConfirmSampling;

    @Column(name = "goal")
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

    @Column(name = "manufacturing_company")
    private String manufacturingCompany;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "doc_number_of_taken_object")
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

    @Column(name = "lis_organization_id")
    private Long lisOrganizationId;

    @Column(name = "laboratory_address")
    private String laboratoryAddress;

    @Embedded
    private PackageTypeInfo packageTypeInfo;

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    @OneToMany(mappedBy = "act154", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id DESC")
    private List<Act154Detail> act154Details = new ArrayList<>();
}
