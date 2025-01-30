package uz.uzinfocom.isemid.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.models.entities.card.Card;
import uz.uzinfocom.isemid.models.entities.template.AbsEntity;

import java.time.LocalDateTime;


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

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    private String hospitalPlace; // Yotqizilgan joyi
    private LocalDateTime admissionDate; // Yotqizilgan sana
    private LocalDateTime diseaseDate; // Kasallangan sana
    private LocalDateTime firstVisitDate; // Birinchi murojaat sanasi
    private LocalDateTime diagnosisDate; // Aniq tashxis qo‘yilgan sanasi
    private LocalDateTime visitDate; // Qatnov sanasi
    private String senderOrganization; // Qayerdan (Yuboruvchi tashkilot)
    private String receiverOrganization; // Qayerga (Qabul qiluvchi tashkilot)
    private String poisoningLocation; // Zaharlangan yoki kasallangan joyi
    private String diseaseCause; // Kasallik sababi
    private String epidemicMeasures; // Epidemiya haqida choralar
    private String comment;
    private String journalForm;
    private String deceaseLocation;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "card_id")
    @JsonIgnoreProperties("form058")
    private Card card;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reporter_user_id")
    @JsonIgnoreProperties({"organizations", "roles", "country", "state", "city", "gender"})
    private User reporter;
}
