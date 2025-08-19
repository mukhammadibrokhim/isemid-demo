package uz.uzinfocom.isemid.features.card.models.card174;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uz.uzinfocom.isemid.commons.models.BaseEntity;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity(name = "card174_external_sample_test")
public class ExternalSampleTest extends BaseEntity {
    private String specimen; // Tekshiruv o‘tkazilgan hayvon turi
    private LocalDate testDate; // Tekshiruv sanasi
    private Integer sampleCount; // Namuna soni
    private String testMethod; // Tekshiruv usuli
    private String testResult; // Tekshiruv natijasi (serovar, qo‘zg‘atuvchi)
}
