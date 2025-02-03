package uz.uzinfocom.isemid.models.payload.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReqLocation {
    @NotNull(message = "Широта (latitude) обязательна.")
    @DecimalMin(value = "-90.0", message = "Широта не может быть меньше -90.0")
    @DecimalMax(value = "90.0", message = "Широта не может быть больше 90.0")
    private Double latitude;

    @NotNull(message = "Долгота (longitude) обязательна.")
    @DecimalMin(value = "-180.0", message = "Долгота не может быть меньше -180.0")
    @DecimalMax(value = "180.0", message = "Долгота не может быть больше 180.0")
    private Double longitude;
    private String location;
}
