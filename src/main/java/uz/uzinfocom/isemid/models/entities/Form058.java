package uz.uzinfocom.isemid.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.models.entities.card.Card;
import uz.uzinfocom.isemid.models.entities.enums.DocumentStatus;
import uz.uzinfocom.isemid.models.entities.template.AbsEntity;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Form058 extends AbsEntity {
    private String mkb10Code;
    private String mkb10Name;
    private Integer mkb10UsageLimit;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    private Boolean labConfirmation; // Tashxisni laborator tasdiqlash

    @ManyToOne
    @JoinColumn(name = "hospital_place", referencedColumnName = "id")
    @JsonIgnoreProperties({"children","serviceArea","city","state","country","level"})
    private Organization hospitalPlace; // Yotqizilgan joyi

    private LocalDateTime admissionDate; // Yotqizilgan sana
    private LocalDateTime diseaseDate; // Kasallangan sana
    private LocalDateTime firstVisitDate; // Birinchi murojaat sanasi
    private LocalDateTime diagnosisDate; // Aniq tashxis qo‘yilgan sanasi
    private LocalDateTime visitDate; // Qatnov sanasi
    private LocalDateTime docSendDate; // document jo'natilgan sana
    
    @ManyToOne
    @JoinColumn(name = "sender_organization_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"children","serviceArea","city","state","country","level"})
    private Organization senderOrganization; // Qayerdan (Yuboruvchi tashkilot)

    @ManyToOne
    @JoinColumn(name = "receiver_organization_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"children","serviceArea","city","state","country","level"})
    private Organization receiverOrganization; // Qayerga (Qabul qiluvchi tashkilot)

    @OneToOne(cascade = CascadeType.ALL)
    @JsonIgnoreProperties("form058")
    private Location location;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "disease_place", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog diseasePlace; // Qayerda zaharlangan yoki kasallangan

    private LocalDateTime initialReportDateTime;

    private String diseaseCause; // Kasallik sababi
    private String epidemicMeasures; // Epidemiya haqida choralar

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "journal_form", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog journalForm;

    private String comment;
    private String notifierFullName;
//
//    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
//    @JoinColumn(name = "card_id")
//    @JsonIgnoreProperties("form058")
//    private Card card;

    @OneToMany(mappedBy = "form058", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("form058")
    @Builder.Default
    private List<Card> cards = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reporter_user_id")
    @JsonIgnoreProperties({"organizations", "roles", "country", "state", "city", "gender"})
    private User reporter;
}
