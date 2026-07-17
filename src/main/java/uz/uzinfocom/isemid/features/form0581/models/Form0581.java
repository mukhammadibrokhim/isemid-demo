package uz.uzinfocom.isemid.features.form0581.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.commons.models.AbsEntity;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.form058.models.enums.FormStatus;
import uz.uzinfocom.isemid.features.patient.models.Patient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "form058_1",
        indexes = {
                @Index(name = "idx_form0581_status", columnList = "status"),
                @Index(name = "idx_form0581_patient_id", columnList = "patient_id"),
                @Index(name = "idx_form0581_sender_org_id", columnList = "sender_organization_id"),
                @Index(name = "idx_form0581_receiver_org_id", columnList = "receiver_organization_id"),
                @Index(name = "idx_form0581_created_at", columnList = "created_at"),
                @Index(name = "idx_form0581_mkb10_code", columnList = "mkb10Code")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Form0581 extends AbsEntity {

    private String mkb10Code; // DE.2
    private String mkb10Name;
    private Integer mkb10UsageLimit;

    private String injuryLocalization; // DE.3

    private String medRecordId; // DE.4

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient; // DE.5-54

    @Column(name = "sender_organization_id")
    private Long senderOrganizationId; // From where (sender organization)

    @Column(name = "receiver_organization_id")
    private Long receiverOrganizationId; // DE.55

    private LocalDateTime injuryDateTime; // DE.56
    private LocalDateTime visitDateTime; // DE.57

    @Embedded
    private InjuryLocation injuryLocation; // DE.58-61

    @OneToOne(cascade = CascadeType.ALL)
    @JsonIgnoreProperties("form0581")
    private Form0581AnimalOwner animalOwner; // DE.62-72

    @Embedded
    private AnimalInfo animalInfo; // DE.73, DE.78-80

    private Boolean otherPeopleBitten; // DE.81

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "form0581", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference("form0581-bittenPersons")
    private List<Form0581BittenPerson> bittenPersons = new ArrayList<>(); // DE.84-93

    private LocalDateTime hospitalizedAt; // DE.94
    private Long hospitalPlaceId; // DE.95-96

    @Column(length = 1000)
    private String antirabicAidInfo; // DE.97

    private String notifierFullName; // DE.98
    private String receiverFullName; // DE.99

    private LocalDateTime initialReportDateTime; // DE.100

    @Enumerated(EnumType.STRING)
    private FormStatus status;

    @Column(name = "source", length = 20)
    private String source;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "form0581", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference("form0581-cards")
    private List<Card> cards = new ArrayList<>();

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "canceled_by")
    private Long canceledBy;

    @Embedded
    private DiagnosisApproval diagnosisApproval;
}
