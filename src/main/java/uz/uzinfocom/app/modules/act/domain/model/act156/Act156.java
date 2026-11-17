package uz.uzinfocom.app.modules.act.domain.model.act156;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.uzinfocom.app.integration.api2.citizen.domain.CitizenLookupType;
import uz.uzinfocom.app.modules.act.domain.model.Act;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "act156")
@NoArgsConstructor
@AllArgsConstructor
public class Act156 extends Act {

    @Column(name = "title")
    private String title;

    @Column(name = "activity_type_code")
    private String activityTypeCode;

    @Column(name = "sample_taken_time")
    private LocalDateTime sampleTakenTime;

    @Column(name = "lis_organization_id")
    private Long lisOrganizationId;

    @Column(name = "laboratory_address")
    private String laboratoryAddress;

    @Column(name = "sample_delivery_time")
    private LocalDateTime sampleDeliveryTime;

    @Column(name = "full_name_of_sampler")
    private String fullNameOfSampler;

    @Column(name = "position_of_sampler")
    private String positionOfSampler;

    @Column(name = "full_name_of_object_representative")
    private String fullNameOfObjectRepresentative;

    @Column(name = "position_of_object_representative")
    private String positionOfObjectRepresentative;

    /**
     * «Hujjat turi» / «Hujjat raqami» of the object representative — the
     * identifier the representative's data was resolved by from the citizen
     * registry, same meaning as {@code EmployeeInfo#identifierType}/{@code identifierValue}
     * on the other act types' {@code participant}.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "identifier_type_of_object_representative", length = 20)
    private CitizenLookupType identifierTypeOfObjectRepresentative;

    @Column(name = "identifier_value_of_object_representative")
    private String identifierValueOfObjectRepresentative;

    @OneToMany(mappedBy = "act156", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Act156KitchenUtensil> act156KitchenUtensils = new ArrayList<>();

    @OneToMany(mappedBy = "act156", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Act156GroupDetail> act156GroupDetails = new ArrayList<>();
}
