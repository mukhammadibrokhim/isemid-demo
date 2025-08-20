package uz.uzinfocom.isemid.features.card.models.card174;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;
import uz.uzinfocom.isemid.commons.models.Vaccination;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity(name = "card174_infection_monitoring")
public class InfectionMonitoring extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "card174_id", nullable = false)
    private Card174 card174;

    private String lastName; // Familiya
    private String firstName; // Ismi
    private String middleName; // Sharifi
    private String genderCode; // Jins kodi (M/F)
    private LocalDate birthDate; // Tug‘ilgan sana
    private String address; // Yashash manzili
    private String profession; // Kasbi yoki ish joyi
    private LocalDate applyDate; // Murojaat qilingan sana
    private LocalDate confirmDate; // Kasallik tasdiqlangan sana
    private String possibleLocation; // Yuqtirgan joy (taxminiy)
    private String possibleFactor; // Yuqtirish omili (taxminiy)
    private LocalDate possibleDate; // Yuqtirilgan sana (taxminiy)
    private LocalDate contactInfectionDate; // Muloqatda bo‘lganlarning kasallanish sanasi

    @ManyToMany
    @JoinTable(
            name = "infection_monitoring_vaccination",
            joinColumns = @JoinColumn(name = "infection_monitoring_id"),
            inverseJoinColumns = @JoinColumn(name = "vaccination_id"))
    private List<Vaccination> vaccinations = new ArrayList<>();
    private String vaccinationSummary;
}
