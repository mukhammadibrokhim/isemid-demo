package uz.uzinfocom.app.modules.act.domain.model.act224;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "act224")
@NoArgsConstructor
@AllArgsConstructor
public class Act224 extends Act {

    @Column(name = "tin")
    private Integer tin;

    @Column(name = "institution_name")
    private String institutionName;

    @Column(name = "institution_address")
    private String institutionAddress;

    @Column(name = "activity_type_code")
    private String activityTypeCode;

    @Column(name = "full_name_of_epid_staff")
    private String fullNameOfEpidStaff;

    @Column(name = "position_of_epid_staff")
    private String positionOfEpidStaff;

    @Column(name = "full_name_of_participant_epid")
    private String fullNameOfParticipantEpid;

    @Column(name = "position_of_participant_epid")
    private String positionOfParticipantEpid;

    @Column(name = "name_of_institution")
    private String nameOfInstitution;

    @Column(name = "address_of_institution")
    private String addressOfInstitution;

    @Column(name = "name_of_regulatory_acts")
    private String nameOfRegulatoryActs;

    @Column(name = "checking_fulfillment_of_requirements", columnDefinition = "TEXT")
    private String checkingFulfillmentOfRequirements;

    @Column(name = "full_name_of_participant")
    private String fullNameOfParticipant;

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    @OneToMany(mappedBy = "act224", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id DESC")
    private List<Act224Detail> act224Details = new ArrayList<>();
}
