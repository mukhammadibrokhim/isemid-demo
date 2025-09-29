package uz.uzinfocom.isemid.features.act.models.act156;

import jakarta.persistence.*;
import lombok.*;
import uz.uzinfocom.isemid.features.act.models.Act;
import uz.uzinfocom.isemid.features.organization.models.Organization;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Act156 extends Act {
    private String title;
    private LocalDate documentSendDate;
    private Integer tin;
    private String institutionName;
    private String institutionAddress;
    private LocalDateTime sampleTakenTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, insertable = false, updatable = false)
    private Organization organization;

    @Column(name = "organization_id")
    private Long organizationId;

    private String laboratoryAddress;
    private LocalDateTime sampleDeliveryTime;
    private String fullNameofSampler;
    private String positionOfSampler;
    private String fullNameOfObjectRepresentative;
    private String positionOfObjectRepresentative;

    @OneToMany(mappedBy = "act156", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Act156KitchenUtensil> act156KitchenUtensils = new ArrayList<>();

    @OneToMany(mappedBy = "act156", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Act156GroupDetail> act156GroupDetails = new ArrayList<>();
}
