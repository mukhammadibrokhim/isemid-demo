package uz.uzinfocom.isemid.models.payload.requests.card.card_tube;


import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqXRay {
    private LocalDate xrayDate;
    private String xrayPlace;
    private String result;
}
