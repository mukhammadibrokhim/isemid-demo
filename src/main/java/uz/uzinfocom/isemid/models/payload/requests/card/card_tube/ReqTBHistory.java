package uz.uzinfocom.isemid.models.payload.requests.card.card_tube;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReqTBHistory {
    private String infectionLocation;
    private LocalDate infectionDate;
    private String mkb10Code;
    private String mkb10Name;
    private String registrationGroup;
}
