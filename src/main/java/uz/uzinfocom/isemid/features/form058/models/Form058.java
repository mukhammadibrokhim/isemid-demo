package uz.uzinfocom.isemid.features.form058.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.commons.models.AbsEntity;
import uz.uzinfocom.isemid.features.card.models.Card;
import uz.uzinfocom.isemid.features.form058.enums.FormStatus;
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
@EntityListeners(AuditingEntityListener.class)
public class Form058 extends AbsEntity {
    private String mkb10Code;
    private String mkb10Name;
    private String finalMkb10Code;
    private String finalMkb10Name;

    private Integer mkb10UsageLimit;

    @Enumerated(EnumType.STRING)
    private FormStatus status;

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
    @JsonManagedReference
    private List<Card> cards = new ArrayList<>();

    private String cardByFullName;

    @Column(length = 1000)
    private String notApproveComment;
}
