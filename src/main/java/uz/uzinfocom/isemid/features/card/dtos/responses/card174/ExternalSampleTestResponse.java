package uz.uzinfocom.isemid.features.card.dtos.responses.card174;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExternalSampleTestResponse {
    private Long id;
    private String specimen; // Tekshiruv o‘tkazilgan hayvon turi
    private LocalDate testDate; // Tekshiruv sanasi
    private Integer sampleCount; // Namuna soni
    private String testMethod; // Tekshiruv usuli
    private String testResult; // Tekshiruv natijasi (serovar, qo‘zg‘atuvchi)
}
