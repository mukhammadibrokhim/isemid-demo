package uz.uzinfocom.isemid.features.card.dtos.requests.card_tube;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TBHistoryRequest {
    private Long id;
    private String infectionLocation;
    private LocalDate infectionDate;
    private String mkb10Code;
    private String mkb10Name;
    private String registrationGroup;
}
