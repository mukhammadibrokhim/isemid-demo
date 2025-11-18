package uz.uzinfocom.isemid.features.patient.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.commons.models.BaseEntity;
import uz.uzinfocom.isemid.features.patient.models.enums.AffiliationType;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "pt_affiliation", indexes = {
        @Index(name = "idx_value_type", columnList = "type"),
        @Index(name = "idx_organization_id", columnList = "organizationId")
})
@EntityListeners(AuditingEntityListener.class)
public class Affiliation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonBackReference
    private Patient patient;

    @Enumerated(EnumType.STRING)
    private AffiliationType type;

    private LocalDate lastVisitedDate;
    private String organizationName;
    private String stateCode;
    private String cityCode;
    private Long organizationId;
    private UUID organizationUuid;
    private String address;
}
