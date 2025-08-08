package uz.uzinfocom.isemid.features.card.dtos.requests.card_tube;


import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqXRay {
    private Long id;
    private LocalDate xrayDate;
    private String xrayPlace;
    private String result;
}
