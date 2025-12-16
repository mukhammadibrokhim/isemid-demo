package uz.uzinfocom.isemid.features.act.models.act156;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.*;
import uz.uzinfocom.isemid.features.act.models.Act;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Act156 extends Act {
    private String title;
    private Integer tin;
    private String institutionName;
    private String institutionAddress;
    private String activityTypeCode;
    private LocalDateTime sampleTakenTime;

    private Long lisOrganizationId;
    private String laboratoryAddress;

    private LocalDateTime sampleDeliveryTime;
    private String fullNameofSampler;
    private String positionOfSampler;
    private String fullNameOfObjectRepresentative;
    private String positionOfObjectRepresentative;

    @OneToMany(mappedBy = "act156", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<Act156KitchenUtensil> act156KitchenUtensils = new HashSet<>();

    @OneToMany(mappedBy = "act156", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Act156GroupDetail> act156GroupDetails = new HashSet<>();
}
