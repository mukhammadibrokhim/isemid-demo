package uz.uzinfocom.isemid.models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uz.uzinfocom.isemid.models.entities.template.BaseEntity;

import java.time.LocalDate;


@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Form058 extends BaseEntity {
    private String mkb10Code;
    private String mkb10Name;
    private String workPlace; // Ish joyi va manzili
    private String hospitalPlace; // Yotqizilgan joyi
    private LocalDate admissionDate; // Yotqizilgan sana
    private LocalDate diseaseDate; // Kasallangan sana
    private LocalDate firstVisitDate; // Birinchi murojaat sanasi
    private LocalDate diagnosisDate; // Aniq tashxis qo‘yilgan sanasi
    private LocalDate visitDate; // Qatnov sanasi
    private String senderOrganization; // Qayerdan (Yuboruvchi tashkilot)
    private String receiverOrganization; // Qayerga (Qabul qiluvchi tashkilot)
    private String poisoningLocation; // Zaharlangan yoki kasallangan joyi
    private String diseaseCause; // Kasallik sababi
    private String epidemicMeasures; // Epidemiya haqida choralar
    private String notifierFullName; // Habar beruvchi F.I.Sh
    private String comment;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
}
