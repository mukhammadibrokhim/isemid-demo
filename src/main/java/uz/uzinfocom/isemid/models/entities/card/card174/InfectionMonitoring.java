package uz.uzinfocom.isemid.models.entities.card.card174;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uz.uzinfocom.isemid.models.entities.template.AbsEntity;
import uz.uzinfocom.isemid.models.entities.value_set.Catalog;

import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "card174_infection_monitoring")
public class InfectionMonitoring extends AbsEntity {

    @ManyToOne
    @JoinColumn(name = "card174_id", nullable = false)
    @JsonIgnore
    private Card174 card174;
    private Integer sequentialNumber;
    private String lastName;
    private String firstName;
    private String middleName;
    @ManyToOne
    @JoinColumn(name = "Gender", referencedColumnName = "code")
    @JsonIgnoreProperties({"type", "id", "createdAt", "updatedAt", "createdBy", "updatedBy"})
    private Catalog gender;
    private LocalDate birthDate;
    private String address;//Manzil.
    private String profession;//Kasbi, ish joyi.
    private LocalDate applicationDate;//Murojat sanasi.
    private LocalDate confirmationDate;//Aniqlangan sana.
    private String possibleInfectionLocation;//Yuqtirilishi mumkin bo‘lgan joyi.
    private String possibleInfectionFactor;//Yuqtirilishi mumkin bo‘lgan yuqish omili.
    private LocalDate possibleInfectionDate;//Yuqtirilishi mumkin bo‘lgan sana.
//    private LocalDate previousInfectionDate;//Ilgari ushbu infeksiyaga qarshi emlangan sana.
//    private String emergencyProphylaxis;
//    private String antibioticTreatmentSeries;
//    private String antibioticTreatmentScheme;  Integration with DMED Emlash.uz

//    private String provizorName; takes from 058
//    private LocalDate provizorRecordingDate;
//    private LocalDate discussionDate;

//    private LocalDate labTestDate; integration with LIS
//    private String labTestResult;
}