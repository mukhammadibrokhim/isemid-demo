package uz.uzinfocom.isemid.models.entities.card;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Card205 extends Card {
    private String specificFieldForCard205;// Faqat Card205 ga xos bo'lgan maydon

    private String epidemiologicalObservationDate; //Epidemiologik surishtiruv o‘tkazilgan sana.
    private String veterinaryEmergencyInformationDate; //Hududiy veterinariya xizmatiga shoshilinch xabar bergan sana.
    private String addressOfBiteOccurrence;//Tishlanish sodir bo‘lgan manzili.
    @Column(name = "date_of_bite_occurence")
    @Temporal(value = TemporalType.DATE)
    private Date dateOfBiteOccurrence; //Tishlanish sodir bo‘lgan sana.
    private String nameOfTreatmentPreventiveInstitution; //Jabrlanganning birinchi bor tibbiy yordamga murojaat qilgan DPM nomi.
    private String dateOfTreatmentPreventiveInstitution; //Jabrlanganning birinchi bor tibbiy yordam uchun DPMga murojaat qilgan sana.
    private String diagnosisOfInjury;//Tashxisi (jarohat joyi). //dropdown
    private Long vaccinationNumber; //Vaksina raqami. //dropdown
    private Date applicationDate;//Murojaat qilgan sanasi.
    private String vaccinationName; //Vaksina nomi.



}
