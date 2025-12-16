package uz.uzinfocom.isemid.features.act.dtos.requests.act224;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Act224DetailRequest {
    private Long id;
    private String recommendedActivity;
    private LocalDateTime executionPeriod;
}

