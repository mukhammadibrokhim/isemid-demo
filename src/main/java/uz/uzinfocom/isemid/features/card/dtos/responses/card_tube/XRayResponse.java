package uz.uzinfocom.isemid.features.card.dtos.responses.card_tube;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class XRayResponse {
    private Long id;
    private LocalDate xrayDate;
    private String xrayPlace;
    private String result;
}
