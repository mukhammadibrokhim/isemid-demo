package uz.uzinfocom.isemid.features.card.dtos.requests.card174;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalSampleTestRequest {
    private Long id;
    private String specimen; // Tekshiruv o‘tkazilgan hayvon turi
    private LocalDate testDate; // Tekshiruv sanasi
    private Integer sampleCount; // Namuna soni
    private String testMethod; // Tekshiruv usuli
    private String testResult; // Tekshiruv natijasi (serovar, qo‘zg‘atuvchi)
}
