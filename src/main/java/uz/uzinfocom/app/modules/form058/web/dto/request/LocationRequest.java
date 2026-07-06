package uz.uzinfocom.app.modules.form058.web.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

public record LocationRequest(
        @DecimalMin(value = "-90.0", message = "{validation.form058.location-latitude.min}")
        @DecimalMax(value = "90.0", message = "{validation.form058.location-latitude.max}")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "{validation.form058.location-longitude.min}")
        @DecimalMax(value = "180.0", message = "{validation.form058.location-longitude.max}")
        Double longitude,

        @Size(max = 1000, message = "{validation.form058.location.size}")
        String location
) {
}