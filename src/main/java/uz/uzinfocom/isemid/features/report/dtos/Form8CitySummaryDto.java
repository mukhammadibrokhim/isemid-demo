package uz.uzinfocom.isemid.features.report.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Form8CitySummaryDto {
    @JsonProperty("city_code")
    private String cityCode;

    @JsonProperty("category_code")
    private String categoryCode;

    private long count;

    @JsonProperty("city_name")
    private String cityName;
}
