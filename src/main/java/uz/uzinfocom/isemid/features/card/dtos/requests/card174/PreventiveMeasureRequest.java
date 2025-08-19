package uz.uzinfocom.isemid.features.card.dtos.requests.card174;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PreventiveMeasureRequest {
    private Long id;
    private String measures;           //chora tadbirlar
    private LocalDate measureDate;     // Chora sanasi
    private Integer measureCount;      // Choralar soni
    private String measureLocation;    // O‘tkazilgan joy
}
