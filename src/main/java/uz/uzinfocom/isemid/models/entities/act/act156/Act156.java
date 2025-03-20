package uz.uzinfocom.isemid.models.entities.act.act156;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.Organization;
import uz.uzinfocom.isemid.models.entities.act.Act;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Act156  extends Act {
    private String title;
    private LocalDate documentSendDate;
    private Integer tin;
    private String institutionName;
    private String institutionAddress;
    private LocalDateTime sampleTakenTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "organization_id", insertable = false, updatable = false)
    private Long organizationId;
    private String laboratoryAddress;
    private LocalDateTime sampleDeliveryTime;
    private String fullNameofSampler;
    private String positionOfSampler;
    private String fullNameOfObjectRepresentative;
    private String positionOfObjectRepresentative;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "act156_kitchen_utensil",
            joinColumns = @JoinColumn(name = "act156_id"),
            inverseJoinColumns = @JoinColumn(name = "act156_utensil_id")
    )
    @JsonIgnore
    private Set<Act156KitchenUtensil> act156KitchenUtensils = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "act156_group_detail",
            joinColumns = @JoinColumn(name = "act156_id"),
            inverseJoinColumns = @JoinColumn(name = "act156_group_detail_id")
    )
    @JsonIgnore
    private Set<Act156GroupDetail> act156GroupDetails = new HashSet<>();
}
