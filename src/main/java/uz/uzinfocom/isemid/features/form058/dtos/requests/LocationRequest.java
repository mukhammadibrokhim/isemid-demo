package uz.uzinfocom.isemid.features.form058.dtos.requests;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationRequest {

    @DecimalMin(value = "-90.0", message = "Широта не может быть меньше -90.0")
    @DecimalMax(value = "90.0", message = "Широта не может быть больше 90.0")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Долгота не может быть меньше -180.0")
    @DecimalMax(value = "180.0", message = "Долгота не может быть больше 180.0")
    private Double longitude;
    private String location;
}
