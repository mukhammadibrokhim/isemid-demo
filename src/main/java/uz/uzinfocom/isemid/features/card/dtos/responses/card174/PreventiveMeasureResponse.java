package uz.uzinfocom.isemid.features.card.dtos.responses.card174;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PreventiveMeasureResponse {
    private Long id;
    private String measures;           //chora Tadbirlar
    private LocalDate measureDate;     // Chora sanasi
    private Integer measureCount;      // Choralar soni
    private String measureLocation;    // O‘tkazilgan joy
}
