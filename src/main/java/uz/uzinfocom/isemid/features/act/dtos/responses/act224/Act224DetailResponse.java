package uz.uzinfocom.isemid.features.act.dtos.responses.act224;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Act224DetailResponse {
    private Long id;
    private String recommendedActivity;
    private LocalDateTime executionPeriod;
}
