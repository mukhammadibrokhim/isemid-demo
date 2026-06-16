package uz.uzinfocom.app.modules.form058.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

@Schema(description = "Form058 location information.")
public record LocationRequest(

        @Schema(
                description = "Geographical latitude.",
                example = "41.311081"
        )
        @DecimalMin(
                value = "-90.0",
                message = "{validation.form058.location.latitude.range}"
        )
        @DecimalMax(
                value = "90.0",
                message = "{validation.form058.location.latitude.range}"
        )
        Double latitude,

        @Schema(
                description = "Geographical longitude.",
                example = "69.240562"
        )
        @DecimalMin(
                value = "-180.0",
                message = "{validation.form058.location.longitude.range}"
        )
        @DecimalMax(
                value = "180.0",
                message = "{validation.form058.location.longitude.range}"
        )
        Double longitude,

        @Schema(
                description = "Location description or address.",
                example = "Toshkent shahri, Yunusobod tumani"
        )
        @Size(
                max = 1000,
                message = "{validation.form058.location.value.size}"
        )
        String location
) {
}