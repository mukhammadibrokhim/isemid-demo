package uz.uzinfocom.isemid.features.form058.models;

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
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "form058",
        indexes = {
                @Index(name = "idx_form058_status", columnList = "status"),
                @Index(name = "idx_form058_patient_id", columnList = "patient_id"),
                @Index(name = "idx_form058_sender_org_id", columnList = "sender_organization_id"),
                @Index(name = "idx_form058_receiver_org_id", columnList = "receiver_organization_id"),
                @Index(name = "idx_form058_created_at", columnList = "created_at"),
                @Index(name = "idx_form058_mkb10_code", columnList = "mkb10Code")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Form058 extends AbsEntity {
    private String mkb10Code;
    private String mkb10Name;
    private String finalMkb10Code;
    private String finalMkb10Name;

    private Integer mkb10UsageLimit;

    @Enumerated(EnumType.STRING)
    private FormStatus status;

    @Column(name = "source", length = 20)
    private String source;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    private Boolean labConfirmation; // Tashxisni laborator tasdiqlash
    private Long hospitalPlaceId; // Yotqizilgan joyi
    private LocalDateTime admissionDate; // Yotqizilgan sana
    private LocalDateTime diseaseDate; // Kasallangan sana
    private LocalDateTime firstVisitDate; // Birinchi murojaat sanasi
    private LocalDateTime diagnosisDate; // Aniq tashxis qo‘yilgan sanasi
    private LocalDateTime visitDate; // Qatnov sanasi
    private LocalDateTime docSendDate; // document jo'natilgan sana

    @Column(name = "sender_organization_id")
    private Long senderOrganizationId; // Qayerdan (Yuboruvchi tashkilot)

    @Column(name = "receiver_organization_id")
    private Long receiverOrganizationId; // Qayerga (Qabul qiluvchi tashkilot)

    @OneToOne(cascade = CascadeType.ALL)
    @JsonIgnoreProperties("form058")
    private Location location;

    private String diseasePlaceCode; // Qayerda zaharlangan yoki kasallangan

    private LocalDateTime initialReportDateTime;

    private String diseaseCause; // Kasallik sababi
    private String epidemicMeasures; // Epidemiya haqida choralar

    private String journalFormCode;

    private String comment;
    private String notifierFullName;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "form058", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference("form058-cards")
    private List<Card> cards = new ArrayList<>();

    private String cardByFullName;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "canceled_by")
    private Long canceledBy;

    @Column(name = "approved_full_name")
    private String approvedFullName;

    @Column(name = "approved_org_uuid")
    private UUID approvedOrgUuid;

    @Column(length = 1000)
    private String notApproveComment;
}
