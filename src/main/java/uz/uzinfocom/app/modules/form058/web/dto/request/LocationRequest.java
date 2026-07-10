package uz.uzinfocom.app.modules.form058.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

@Schema(description = "Географическое место выявления заболевания.")
public record LocationRequest(
        @Schema(description = "Широта.")
        @DecimalMin(value = "-90.0", message = "{validation.form058.location-latitude.min}")
        @DecimalMax(value = "90.0", message = "{validation.form058.location-latitude.max}")
        Double latitude,

        @Schema(description = "Долгота.")
        @DecimalMin(value = "-180.0", message = "{validation.form058.location-longitude.min}")
        @DecimalMax(value = "180.0", message = "{validation.form058.location-longitude.max}")
        Double longitude,

        @Schema(description = "Текстовое описание места (адрес).")
        @Size(max = 1000, message = "{validation.form058.location.size}")
        String location
) {
}
