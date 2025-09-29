package uz.uzinfocom.isemid.features.act.dtos.responses.act224;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendedActionResponse {
    private Long id;
    private String recommendedActivity; // Taklif etilayotgan chora-tadbir
    private LocalDateTime executionPeriod; // Amalga oshirish muddati
}
