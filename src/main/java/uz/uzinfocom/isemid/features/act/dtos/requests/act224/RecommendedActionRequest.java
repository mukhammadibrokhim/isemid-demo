package uz.uzinfocom.isemid.features.act.dtos.requests.act224;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendedActionRequest {
    private Long id; // mavjud bo‘lishi shart, update uchun
    private String recommendedActivity;
    private LocalDateTime executionPeriod;
}
