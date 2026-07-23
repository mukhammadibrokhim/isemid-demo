package uz.uzinfocom.app.modules.act.domain.model.act156;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @Column(name = "tin")
    private Integer tin;

    @Column(name = "institution_name")
    private String institutionName;

    @Column(name = "institution_address")
    private String institutionAddress;

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

    @OneToMany(mappedBy = "act156", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Act156KitchenUtensil> act156KitchenUtensils = new ArrayList<>();

    @OneToMany(mappedBy = "act156", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Act156GroupDetail> act156GroupDetails = new ArrayList<>();
}
