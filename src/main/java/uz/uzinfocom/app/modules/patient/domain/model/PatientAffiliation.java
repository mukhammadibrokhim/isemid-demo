package uz.uzinfocom.app.modules.patient.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.app.modules.patient.domain.enums.AffiliationType;
import uz.uzinfocom.app.platform.persistence.entity.AbsEntity;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "pt_affiliation",
        indexes = {
                @Index(
                        name = "idx_value_type",
                        columnList = "type"
                ),
                @Index(
                        name = "idx_organization_id",
                        columnList = "organization_id"
                ),
                @Index(
                        name = "idx_pt_affiliation_patient_id",
                        columnList = "patient_id"
                ),
                @Index(
                        name = "idx_pt_affiliation_patient_org_type",
                        columnList = "patient_id,organization_id,type"
                )
        }
)
public class PatientAffiliation extends AbsEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AffiliationType type;

    @Column(name = "last_visited_date")
    private LocalDate lastVisitedDate;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "region_code")
    private String regionCode;

    @Column(name = "district_code")
    private String districtCode;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "organization_uuid")
    private UUID organizationUuid;

    @Column(name = "address")
    private String address;
}
